package com.fatec.mercadolivre.model.entidades;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "clientes")
public class Cliente {

    @Id
    private String id;
    private String nome;
    private String email;
    private String cpf;
    private String senha;

    private LocalDateTime createAt;
    private List<Endereco> enderecos;

    private List<Produto> favoritos;

}