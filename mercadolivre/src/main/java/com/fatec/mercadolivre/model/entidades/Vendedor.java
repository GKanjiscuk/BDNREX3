package com.fatec.mercadolivre.model.entidades;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Data
@Document(collection = "vendedores")
public class Vendedor {

    @Id
    private String id;

    private String nome;
    private String cpf;
    private String nomeLoja;
    private String cnpj;
    private String dataNasc;
    private String telefone;
    private String email;
    private String senha;

    private List<Endereco> enderecos;

}