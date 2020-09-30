package com.laura.springbootwebflux.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.laura.springbootwebflux.dao.CategoriaDao;
import com.laura.springbootwebflux.dao.ProductoDao;
import com.laura.springbootwebflux.model.documents.Categoria;
import com.laura.springbootwebflux.model.documents.Producto;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductoServiceImpl implements ProductoService{
	
	@Autowired
	ProductoDao productoDao;
	
	@Autowired
	CategoriaDao categoriaDao;

	@Override
	public Flux<Producto> findAll() {
		return productoDao.findAll();
	}

	@Override
	public Mono<Producto> findById(String id) {
		return productoDao.findById(id);
	}

	@Override
	public Mono<Producto> save(Producto producto) {
		return productoDao.save(producto);
	}

	@Override
	public Mono<Void> delete(Producto producto) {
		return productoDao.delete(producto);
	}

	@Override
	public Flux<Producto> findAllNameUpperCase() {
		return productoDao.findAll()
				.map(producto -> {
					producto.setNombre(producto.getNombre().toUpperCase());
					return producto;
				});
	}

	@Override
	public Flux<Producto> findAllNameUpperCaseRepeat() {
		return findAllNameUpperCase().repeat(5000);
	}

	@Override
	public Flux<Categoria> findAllCategoria() {
		return categoriaDao.findAll();
	}

	@Override
	public Mono<Categoria> findCategoriaById(String id) {
		return categoriaDao.findById(id);
	}

	@Override
	public Mono<Categoria> save(Categoria categoria) {
		return categoriaDao.save(categoria);
	}
	

}
