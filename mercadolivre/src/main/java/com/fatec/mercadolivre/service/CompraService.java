package com.fatec.mercadolivre.service;

import com.fatec.mercadolivre.model.entidades.Cliente;
import com.fatec.mercadolivre.model.entidades.Compra;
import com.fatec.mercadolivre.model.entidades.ItemCompra;
import com.fatec.mercadolivre.repository.ClienteRepository;
import com.fatec.mercadolivre.repository.CompraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CompraService {

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    public Compra registrarNovaCompra(String clienteId, List<ItemCompra> itens) {
        Optional<Cliente> clienteOpt = clienteRepository.findById(clienteId);

        if (!clienteOpt.isPresent()) {
            throw new IllegalArgumentException("Cliente com ID " + clienteId + " não encontrado.");
        }

        double totalCompra = itens.stream()
                .mapToDouble(item -> item.getPrecoUnitario() * item.getQuantidade())
                .sum();

        Compra novaCompra = new Compra();
        novaCompra.setCliente(clienteOpt.get());
        novaCompra.setProdutos(itens);
        novaCompra.setTotal(totalCompra);
        novaCompra.setDataCompra(LocalDateTime.now());

        return compraRepository.save(novaCompra);
    }

    public Optional<Compra> buscarPorId(String id) {
        return compraRepository.findById(id);
    }

    public List<Compra> listarTodas() {
        return compraRepository.findAll();
    }

    public List<Compra> listarComprasPorCliente(String clienteId) {
        return compraRepository.findByClienteId(clienteId);
    }

    public boolean deletarCompra(String id) {
        if (compraRepository.existsById(id)) {
            compraRepository.deleteById(id);
            return true;
        }
        return false;
    }


}