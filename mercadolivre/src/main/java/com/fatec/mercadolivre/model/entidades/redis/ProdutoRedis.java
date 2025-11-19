package com.fatec.mercadolivre.model.entidades.redis;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.time.LocalDateTime;

@Data
@RedisHash("produtos")
public class ProdutoRedis {

    @Id
    private String id;
    private String nome;
    private String descricao;
    private Double preco;
    private String vendedorId;
    private LocalDateTime createAt;
}