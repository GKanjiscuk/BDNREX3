package com.fatec.mercadolivre.service;

import com.fatec.mercadolivre.model.entidades.Cliente;
import com.fatec.mercadolivre.model.entidades.Endereco;
import com.fatec.mercadolivre.model.entidades.Produto;
import com.fatec.mercadolivre.model.entidades.redis.ProdutoRedis;
import com.fatec.mercadolivre.repository.ClienteRepository;
import com.fatec.mercadolivre.repository.ProdutoRepository;
import com.fatec.mercadolivre.repository.ProdutoRedisRepository;
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

    @Autowired
    private ProdutoRedisRepository produtoRedisRepository;

    @Autowired private SecurityService securityService;

    public Cliente criarCliente(Cliente cliente, List<Endereco> enderecos) {
        cliente.setCreateAt(LocalDateTime.now());

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
            if (novosDados.getNome() != null) clienteAtual.setNome(novosDados.getNome());
            if (novosDados.getEmail() != null) clienteAtual.setEmail(novosDados.getEmail());
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

        Produto produtoParaAdicionar = null;

        Optional<Produto> mongoOpt = produtoRepository.findById(produtoId);
        if (mongoOpt.isPresent()) {
            produtoParaAdicionar = mongoOpt.get();
        }
        else {
            Optional<ProdutoRedis> redisOpt = produtoRedisRepository.findById(produtoId);
            if (redisOpt.isPresent()) {
                ProdutoRedis pr = redisOpt.get();
                produtoParaAdicionar = new Produto();
                produtoParaAdicionar.setId(pr.getId());
                produtoParaAdicionar.setNome(pr.getNome());
                produtoParaAdicionar.setPreco(pr.getPreco());
                produtoParaAdicionar.setDescricao(pr.getDescricao());

            }
        }

        if (clienteOpt.isPresent() && produtoParaAdicionar != null) {
            Cliente cliente = clienteOpt.get();

            if (cliente.getFavoritos() == null) {
                cliente.setFavoritos(new ArrayList<>());
            }

            Produto finalProduto = produtoParaAdicionar;
            boolean jaExiste = cliente.getFavoritos().stream()
                    .anyMatch(f -> f.getId().equals(finalProduto.getId()));

            if (!jaExiste) {
                cliente.getFavoritos().add(produtoParaAdicionar);
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