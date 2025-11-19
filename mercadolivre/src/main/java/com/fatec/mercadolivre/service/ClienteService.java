package com.fatec.mercadolivre.service;

import com.fatec.mercadolivre.model.entidades.Cliente;
import com.fatec.mercadolivre.model.entidades.Endereco;
import com.fatec.mercadolivre.model.entidades.Produto;
import com.fatec.mercadolivre.repository.ClienteRepository;
import com.fatec.mercadolivre.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.ArrayList;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired private SecurityService securityService;

    public Cliente criarCliente(Cliente cliente, List<Endereco> enderecos) {
        cliente.setCreateAt(LocalDateTime.now());

        // CORREÇÃO: Agora 'securityService' é reconhecido e a senha é criptografada
        if (cliente.getSenha() != null && !cliente.getSenha().isEmpty()) {
            String senhaHash = securityService.criptografarSenha(cliente.getSenha());
            cliente.setSenha(senhaHash);
        }

        boolean principalDefinido = enderecos.stream().anyMatch(Endereco::isPrincipal);
        if (!principalDefinido && !enderecos.isEmpty()) {
            enderecos.get(0).setPrincipal(true);
        }

        enderecos.forEach(e -> e.setEnderecoId(UUID.randomUUID().toString()));
        cliente.setEnderecos(enderecos);

        cliente.setFavoritos(new ArrayList<>());

        return clienteRepository.save(cliente);
    }
    public Cliente atualizarCliente(String id, Cliente novosDados) {
        Optional<Cliente> clienteOpt = clienteRepository.findById(id);

        if (clienteOpt.isPresent()) {
            Cliente clienteAtual = clienteOpt.get();

            if (novosDados.getNome() != null) {
                clienteAtual.setNome(novosDados.getNome());
            }
            if (novosDados.getEmail() != null) {
                clienteAtual.setEmail(novosDados.getEmail());
            }

            return clienteRepository.save(clienteAtual);
        }
        return null;
    }

    public boolean deletarCliente(String id) {
        if (clienteRepository.existsById(id)) {
            clienteRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }

    public Optional<Cliente> buscarPorId(String id) {
        return clienteRepository.findById(id);
    }

    public boolean adicionarFavorito(String clienteId, String produtoId) {
        Optional<Cliente> clienteOpt = clienteRepository.findById(clienteId);
        Optional<Produto> produtoOpt = produtoRepository.findById(produtoId);

        if (clienteOpt.isPresent() && produtoOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();
            Produto produto = produtoOpt.get();

            if (cliente.getFavoritos() == null) {
                cliente.setFavoritos(new ArrayList<>());
            }

            boolean jaExiste = cliente.getFavoritos().stream()
                    .anyMatch(f -> f.getId().equals(produtoId));

            if (!jaExiste) {
                cliente.getFavoritos().add(produto);
                clienteRepository.save(cliente);
                return true;
            }
        }
        return false;
    }

    public boolean removerFavorito(String clienteId, String produtoId) {
        Optional<Cliente> clienteOpt = clienteRepository.findById(clienteId);

        if (clienteOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();
            if (cliente.getFavoritos() == null) return false;

            boolean removido = cliente.getFavoritos().removeIf(produto -> produto.getId().equals(produtoId));

            if (removido) {
                clienteRepository.save(cliente);
                return true;
            }
        }
        return false;
    }
}