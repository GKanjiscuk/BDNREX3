package com.fatec.mercadolivre.model.entidades;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "produtos")
public class Produto {

    @Id
    private String id;
    private String nome;
    private String descricao;
    private Double preco;

    @DBRef
    private Vendedor vendedor;

    private LocalDateTime createAt = LocalDateTime.now();
}