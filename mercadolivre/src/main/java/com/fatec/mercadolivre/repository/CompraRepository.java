package com.fatec.mercadolivre.repository;

import com.fatec.mercadolivre.model.entidades.Compra;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface CompraRepository extends MongoRepository<Compra, String> {
    List<Compra> findByClienteId(String clienteId);
    List<Compra> findByTotalGreaterThan(Double total);
}