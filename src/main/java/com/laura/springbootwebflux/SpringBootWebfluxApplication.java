package com.laura.springbootwebflux;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import com.laura.springbootwebflux.model.documents.Categoria;
import com.laura.springbootwebflux.model.documents.Producto;
import com.laura.springbootwebflux.services.ProductoService;

import reactor.core.publisher.Flux;

@SpringBootApplication
public class SpringBootWebfluxApplication implements CommandLineRunner{
	
	private static final Logger log = LoggerFactory.getLogger(SpringBootWebfluxApplication.class);
	
	@Autowired
	ProductoService service;
	
	@Autowired
	ReactiveMongoTemplate reactiveMongoTemplate;

	public static void main(String[] args) {
		SpringApplication.run(SpringBootWebfluxApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		reactiveMongoTemplate.dropCollection("productos").subscribe();
		reactiveMongoTemplate.dropCollection("categorias").subscribe();
		
		Categoria electronico = new Categoria("electronico");
		Categoria deporte = new Categoria("deporte");
		Categoria computacion = new Categoria("computacion");
		Categoria mueble = new Categoria("muebles");
		
		Flux.just(electronico, deporte, computacion, mueble)
		.flatMap(c -> service.save(c))
		.doOnNext(c -> log.info("categoria creada: "+c.getNombre()))
		.thenMany(
				Flux.just(
						new Producto("Televisor", 200.4, electronico),
						new Producto("Lavadora", 300.5, electronico),
						new Producto("Nevera", 400.6, electronico),
						new Producto("Mueble", 500.6, mueble),
						new Producto("Cojin", 400.3, mueble),
						new Producto("Bicicleta", 400.3, deporte),
						new Producto("Smartphone", 540.5, computacion)
						)
				.flatMap(producto -> {
					producto.setCreateAt(new Date());
					return service.save(producto);
				}))		
		.subscribe(producto -> log.info(producto.getId()+" nombre: "+producto.getNombre()));
		
	}

}
