package com.fatec.mercadolivre.repository;


import com.fatec.mercadolivre.model.entidades.Cliente;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;
import java.util.List;

public interface ClienteRepository extends MongoRepository<Cliente, String> {
    Optional<Cliente> findByEmail(String email);
    List<Cliente> findByNomeIgnoreCase(String nome);
}