package com.laura.springbootwebflux.controllers;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;

import com.laura.springbootwebflux.model.documents.Categoria;
import com.laura.springbootwebflux.model.documents.Producto;
import com.laura.springbootwebflux.services.ProductoService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SessionAttributes("producto")
@Controller
public class ProductoController {

	private static final Logger log = LoggerFactory.getLogger(ProductoController.class);
	
	@Autowired
	private ProductoService productoService;
	
	@Value("${config.uploads.path}")
	private String path;
	
	@ModelAttribute("categorias")
	public Flux<Categoria> categorias(){
		return productoService.findAllCategoria();
	}
	
	@GetMapping("/uploads/img/{nombreFoto:.+}")
	public Mono<ResponseEntity<Resource>> verFoto(@PathVariable String nombreFoto) throws MalformedURLException{
		Path ruta = Paths.get(path).resolve(nombreFoto).toAbsolutePath();
		
		Resource imagen = new UrlResource(ruta.toUri());
		
		return Mono.just(
				ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + imagen.getFilename() + "\"")
				.body(imagen)
				);
		
	}

	@GetMapping("/ver/{id}")
	public Mono<String> ver(Model model, @PathVariable String id){
		
		return productoService.findById(id)
				.doOnNext(p -> {
					model.addAttribute("producto", p);
					model.addAttribute("titulo", "Detalle Producto");
				}).switchIfEmpty(Mono.just(new Producto()))
				.flatMap(p->{
					if(p.getId() == null) {
						return Mono.error(new InterruptedException("No extiste el producto"));
					}
					return Mono.just(p);
				}).then(Mono.just("ver"))
				.onErrorResume(ex -> Mono.just("redirect:/listar?error=no+existe+el+producto"));
	}

	
	@GetMapping({"/listar","/"})
	public Mono<String> listar(Model model) {
		Flux<Producto> productos = productoService.findAllNameUpperCase();
		
		productos.subscribe(prod -> log.info(prod.getNombre()));
		
		model.addAttribute("productos",productos);
		model.addAttribute("titulo", "Listado de productos");
		return Mono.just("listar");
	}
	
	@GetMapping("/form")
	public Mono<String> crear(Model model){
		model.addAttribute("producto", new Producto());
		model.addAttribute("boton", "Crear");
		model.addAttribute("titulo","Formulario de producto");
		return Mono.just("form");
	}
	
	@PostMapping("/form")
	public Mono<String> guardar(@Valid Producto producto, BindingResult result, Model model, @RequestPart FilePart file,
			SessionStatus status) {

		if (result.hasErrors()) {
			model.addAttribute("titulo", "Errores en formulario producto");
			model.addAttribute("boton", "Guardar");
			return Mono.just("form");
		} else {
			status.setComplete();

			Mono<Categoria> categoria = productoService.findCategoriaById(producto.getCategoria().getId());

			return categoria.flatMap(c -> {
				if (producto.getCreateAt() == null) {
					producto.setCreateAt(new Date());
				}

				if (!file.filename().isEmpty()) {
					producto.setFoto(UUID.randomUUID().toString() + "-"
							+ file.filename().replace(" ", "").replace(":", "").replace("\\", ""));
				}
				producto.setCategoria(c);
				return productoService.save(producto);
			}).doOnNext(p -> {
				log.info(
						"Categoria asignada: " + p.getCategoria().getNombre() + " Id Cat: " + p.getCategoria().getId());
				log.info("Producto guardado: " + p.getNombre() + " Id: " + p.getId());
			})
			.flatMap(p -> {
				if(!file.filename().isEmpty()) {
					//return file.transferTo(new File(path+p.getFoto()));
				}
				return Mono.empty();
			})		
					.thenReturn("redirect:/listar?success=producto+guardado+con+exito");
		}
	}

	
	@GetMapping("/form/{id}")
	public Mono<String> editar(@PathVariable String id, Model model){
		Mono<Producto> productoMono = productoService.findById(id)
				.doOnNext(p -> log.info("Producto"+p.getId()+"-"+p.getNombre()))
				.defaultIfEmpty(new Producto());
		
		model.addAttribute("producto", productoMono);
		model.addAttribute("boton", "Editar");
		model.addAttribute("titulo", "Editar producto");
		return Mono.just("form");
	}
	
	@GetMapping("/form-v2/{id}")
	public Mono<String> editarV2(@PathVariable String id, Model model){
		return productoService.findById(id)
				.doOnNext(p -> {
					log.info("Producto"+p.getId()+"-"+p.getNombre());
					model.addAttribute("producto", p);
					model.addAttribute("boton", "Editar");
					model.addAttribute("titulo", "Editar producto");
				})
				.flatMap(p -> {
					if(p.getId() == null) {
						return Mono.error(new InterruptedException("No existe el producto"));
					}
					return Mono.just(p);
					
				})
				.then(Mono.just("form"))
				.onErrorResume(ex -> Mono.just("redirect:/listar?no+existe+el+producto"));
	}
	
	@GetMapping("/listar-datadriver")
	public String listarDataDriver(Model model) {
		Flux<Producto> productos = productoService.findAllNameUpperCase().delayElements(Duration.ofSeconds(1));
		
		productos.subscribe(prod -> log.info(prod.getNombre()));
		
		model.addAttribute("productos",new ReactiveDataDriverContextVariable(productos,2));
		model.addAttribute("titulo", "Listado de productos");
		return "listar";
	}
	
	@GetMapping("/listar-full")
	public String listarFull(Model model) {
		Flux<Producto> productos = productoService.findAllNameUpperCaseRepeat();
		
		productos.subscribe(prod -> log.info(prod.getNombre()));
		
		model.addAttribute("productos",productos);
		model.addAttribute("titulo", "Listado de productos");
		return "listar";
	}
	
	@GetMapping("/listar-chunked")
	public String listarChunked(Model model) {
		Flux<Producto> productos = productoService.findAllNameUpperCaseRepeat();
		
		productos.subscribe(prod -> log.info(prod.getNombre()));
		
		model.addAttribute("productos",productos);
		model.addAttribute("titulo", "Listado de productos");
		return "listar-chunked";
	}
	
	@GetMapping("/eliminar/{id}")
	public Mono<String> eliminar(@PathVariable String id) {
		return productoService.findById(id)
				.flatMap(p -> {
					if(p.getId() == null) {
						return Mono.error(new InterruptedException("No existe el producto a eliminar"));
					}
					return Mono.just(p);
					
				})
				.flatMap(p -> {
					log.info("Eliminando el producto: "+p.getNombre());
			return productoService.delete(p);
		}).then(Mono.just("redirect:/listar?success=producto+eliminado+con+exito"))
		  .onErrorResume(ex -> Mono.just("redirect:/listar?no+existe+el+producto"));
	}
}
