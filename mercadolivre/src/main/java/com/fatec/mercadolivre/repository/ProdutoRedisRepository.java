package com.fatec.mercadolivre.repository;

import com.fatec.mercadolivre.model.entidades.redis.ProdutoRedis;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ProdutoRedisRepository extends CrudRepository<ProdutoRedis, String> {

    List<ProdutoRedis> findByVendedorId(String vendedorId);
}