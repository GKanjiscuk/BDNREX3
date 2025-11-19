package com.fatec.mercadolivre.repository;

import com.fatec.mercadolivre.model.entidades.Produto;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ProdutoRepository extends MongoRepository<Produto, String> {
    List<Produto> findByNomeIgnoreCase(String nome);
    List<Produto> findByVendedorId(String vendedorId);
    List<Produto> findByPrecoGreaterThanEqual(Double preco);
}