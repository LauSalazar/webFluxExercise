package com.laura.springbootwebflux.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.laura.springbootwebflux.model.documents.Producto;
import com.laura.springbootwebflux.services.ProductoService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/productos")
public class ProductoRestController {
	private static final Logger log = LoggerFactory.getLogger(ProductoController.class);
	
	@Autowired
	private ProductoService productoService;
	
	@GetMapping()
	public Flux<Producto> index(){
		Flux<Producto> productos = productoService.findAllNameUpperCase()
				.doOnNext( producto -> log.info(producto.getId()+"-"+producto.getNombre()));
		return productos;
	}
	
	@GetMapping("/{id}")
	public Mono<Producto> getProductoById(@PathVariable String id){
		//Mono<Producto> producto = productoDao.findById(id);
		Flux<Producto> productos = productoService.findAll();
		Mono<Producto> producto = productos.filter(p -> {
			return id.equalsIgnoreCase(p.getId());
		})
		.next()
		.doOnNext( p -> log.info(p.getId()+"-"+p.getNombre()));;
		
		return producto;
	}

}
