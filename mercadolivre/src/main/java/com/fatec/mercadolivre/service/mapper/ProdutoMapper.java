package com.fatec.mercadolivre.service.mapper;

import com.fatec.mercadolivre.model.entidades.Produto;
import com.fatec.mercadolivre.model.entidades.Vendedor;
import com.fatec.mercadolivre.model.entidades.redis.ProdutoRedis;

import java.time.LocalDateTime;
import java.util.UUID;

public class ProdutoMapper {

    public static ProdutoRedis toRedis(Produto mongo, String vendedorId) {
        ProdutoRedis redis = new ProdutoRedis();
        redis.setId(mongo.getId() != null ? mongo.getId() : UUID.randomUUID().toString());
        redis.setNome(mongo.getNome());
        redis.setDescricao(mongo.getDescricao());
        redis.setPreco(mongo.getPreco());
        redis.setVendedorId(vendedorId);
        redis.setCreateAt(LocalDateTime.now());
        return redis;
    }

    public static Produto toMongo(ProdutoRedis redis, Vendedor vendedor) {
        Produto mongo = new Produto();
        mongo.setId(redis.getId());
        mongo.setNome(redis.getNome());
        mongo.setDescricao(redis.getDescricao());
        mongo.setPreco(redis.getPreco());
        mongo.setCreateAt(redis.getCreateAt());
        mongo.setVendedor(vendedor);
        return mongo;
    }
}