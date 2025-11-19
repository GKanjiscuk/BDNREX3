package com.fatec.mercadolivre.model.entidades;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "compras")
public class Compra {

    @Id
    private String id;

    @DBRef
    private Cliente cliente;

    private List<ItemCompra> produtos;

    private Double total;
    private LocalDateTime dataCompra = LocalDateTime.now();
}