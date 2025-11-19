package com.fatec.mercadolivre.service;

import com.fatec.mercadolivre.model.entidades.Produto;
import com.fatec.mercadolivre.model.entidades.Vendedor;
import com.fatec.mercadolivre.model.entidades.redis.ProdutoRedis;
import com.fatec.mercadolivre.repository.ProdutoRepository;
import com.fatec.mercadolivre.repository.ProdutoRedisRepository; // NOVO
import com.fatec.mercadolivre.repository.VendedorRepository;
import com.fatec.mercadolivre.service.mapper.ProdutoMapper; // NOVO
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProdutoService {

    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private VendedorRepository vendedorRepository;
    @Autowired private ProdutoRedisRepository produtoRedisRepository; // INJETADO

    // --- MÉTODOS DE CRIAÇÃO E MANIPULAÇÃO (AGORA REDIS-FIRST) ---

    /**
     * Cria um novo produto, salvando-o PRIMEIRO no Redis.
     * @return ProdutoRedis (o objeto em cache)
     */
    public ProdutoRedis criarProduto(Produto produto, String vendedorId) {
        Optional<Vendedor> vendedorOpt = vendedorRepository.findById(vendedorId);

        if (vendedorOpt.isEmpty()) {
            throw new IllegalArgumentException("Vendedor com ID " + vendedorId + " não encontrado.");
        }

        // Usa o Mapper para converter de Mongo para Redis
        ProdutoRedis redisProduto = ProdutoMapper.toRedis(produto, vendedorId);

        // SALVA NO REDIS (Cache-First)
        return produtoRedisRepository.save(redisProduto);
    }

    /**
     * Atualiza um produto. Busca e manipula APENAS no Redis.
     * @return ProdutoRedis atualizado ou null
     */
    public ProdutoRedis atualizarProdutoRedis(String id, Produto novosDados) {
        // Busca a versão em cache
        Optional<ProdutoRedis> produtoOpt = produtoRedisRepository.findById(id);

        if (produtoOpt.isPresent()) {
            ProdutoRedis produtoAtual = produtoOpt.get();

            if (novosDados.getNome() != null) {
                produtoAtual.setNome(novosDados.getNome());
            }
            if (novosDados.getPreco() != null) {
                produtoAtual.setPreco(novosDados.getPreco());
            }

            // Salva de volta no Redis
            return produtoRedisRepository.save(produtoAtual);
        }
        return null;
    }

    /**
     * Deleta um produto, removendo-o do Redis.
     */
    public boolean deletarProdutoRedis(String id) {
        if (produtoRedisRepository.existsById(id)) {
            produtoRedisRepository.deleteById(id);
            return true;
        }
        // Retornamos falso se não estiver no Redis, a sincronização cuidará do item Mongo.
        return false;
    }

    // --- MÉTODOS DE CONSULTA (AGORA REDIS-FIRST) ---

    /**
     * Lista todos os produtos DO CACHE (Redis)
     */
    public List<ProdutoRedis> listarTodosRedis() {
        return (List<ProdutoRedis>) produtoRedisRepository.findAll();
    }

    /**
     * Busca um produto PRIMEIRO no Redis
     */
    public Optional<ProdutoRedis> buscarPorIdRedis(String id) {
        return produtoRedisRepository.findById(id);
    }

    /**
     * Busca por vendedor no cache (Redis)
     */
    public List<ProdutoRedis> findByVendedorIdRedis(String vendedorId) {
        return produtoRedisRepository.findByVendedorId(vendedorId);
    }

    // --- MÉTODOS DE FALLBACK PARA O MONGO (Usados pelo Seeder e Compra) ---

    // NOTA: Estes métodos são necessários para o DatabaseSeeder e a Compra (que precisa do objeto Mongo no ItemCompra)

    public Optional<Produto> buscarPorIdMongo(String id) {
        return produtoRepository.findById(id);
    }

    public List<Produto> listarTodosMongo() {
        return produtoRepository.findAll();
    }
}