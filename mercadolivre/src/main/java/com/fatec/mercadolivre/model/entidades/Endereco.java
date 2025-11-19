package com.fatec.mercadolivre.model.entidades;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
public class Endereco {

    private String enderecoId;

    private String estado;
    private String cidade;
    private String cep;

    private boolean principal;
}