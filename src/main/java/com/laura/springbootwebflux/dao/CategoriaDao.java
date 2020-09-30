package com.laura.springbootwebflux.dao;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.laura.springbootwebflux.model.documents.Categoria;

public interface CategoriaDao extends ReactiveMongoRepository<Categoria, String>{

}
