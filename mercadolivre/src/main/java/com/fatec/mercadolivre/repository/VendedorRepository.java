package com.fatec.mercadolivre.repository;

import com.fatec.mercadolivre.model.entidades.Vendedor;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface VendedorRepository extends MongoRepository<Vendedor, String> {
    Optional<Vendedor> findByCnpj(String cnpj);
    Optional<Vendedor> findByNomeLojaIgnoreCase(String nomeLoja);
    Optional<Vendedor> findByEmail(String email);
}