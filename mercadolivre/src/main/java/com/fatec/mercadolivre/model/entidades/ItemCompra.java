package com.fatec.mercadolivre.model.entidades;

import lombok.Data;

@Data
public class ItemCompra {
    private String produtoId;
    private String nomeProduto;
    private int quantidade;
    private Double precoUnitario;
    private String vendedorId;
}