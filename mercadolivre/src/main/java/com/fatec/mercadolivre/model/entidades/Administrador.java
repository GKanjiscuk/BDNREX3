package com.fatec.mercadolivre.model.entidades;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "administradores")
public class Administrador {
    @Id
    private String id;
    private String nome;
    private String email;
    private String senha;
}