package com.fatec.mercadolivre.repository;

import com.fatec.mercadolivre.model.entidades.Administrador;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface AdministradorRepository extends MongoRepository<Administrador, String> {
    Optional<Administrador> findByEmail(String email);
}