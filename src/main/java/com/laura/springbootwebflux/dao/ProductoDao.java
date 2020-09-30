package com.laura.springbootwebflux.dao;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.laura.springbootwebflux.model.documents.Producto;

public interface ProductoDao extends ReactiveMongoRepository<Producto, String>{

}
