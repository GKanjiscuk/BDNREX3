package com.fatec.mercadolivre.service;

import com.fatec.mercadolivre.model.entidades.Produto;
import com.fatec.mercadolivre.model.entidades.redis.ProdutoRedis;
import com.fatec.mercadolivre.model.entidades.Vendedor;
import com.fatec.mercadolivre.repository.ProdutoRepository;
import com.fatec.mercadolivre.repository.ProdutoRedisRepository;
import com.fatec.mercadolivre.repository.VendedorRepository;
import com.fatec.mercadolivre.service.mapper.ProdutoMapper; // NOVO IMPORT
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SincronizacaoService {

    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private ProdutoRedisRepository produtoRedisRepository;
    @Autowired private VendedorRepository vendedorRepository;

    // ... (sincronizarRedisParaMongo permanece o mesmo)

    public int sincronizarRedisParaMongo() {
        // 1. Pega todos os itens do Redis
        List<ProdutoRedis> produtosRedis = (List<ProdutoRedis>) produtoRedisRepository.findAll();
        int count = 0;

        for (ProdutoRedis redisItem : produtosRedis) {

            // Resolve a referência do Vendedor (DBRef)
            Optional<Vendedor> vendedorOpt = vendedorRepository.findById(redisItem.getVendedorId());
            if (vendedorOpt.isEmpty()) {
                // Se o vendedor não for encontrado, ignora o item ou marca para exclusão
                System.err.println("Vendedor não encontrado para o produto Redis ID: " + redisItem.getId());
                continue;
            }

            // Converte o ProdutoRedis de volta para Produto (Mongo) USANDO O MAPPER
            Produto mongoProduto = ProdutoMapper.toMongo(redisItem, vendedorOpt.get());

            // 2. Salva/Atualiza no MongoDB (Insert ou Update)
            produtoRepository.save(mongoProduto);
            count++;
        }

        // 3. Limpa o cache do Redis
        produtoRedisRepository.deleteAll();

        return count;
    }

    /**
     * Função inicial para mover itens do Mongo para o Redis (sync to redis)
     * (Usada para carregar itens antes da manipulação, conforme o EX3)
     */
    public int migrarProdutosMongoParaRedis(List<String> ids) {
        int count = 0;
        for (String id : ids) {
            Optional<Produto> produtoOpt = produtoRepository.findById(id);
            if (produtoOpt.isPresent()) {
                Produto p = produtoOpt.get();
                // Converte e salva no Redis USANDO O MAPPER
                ProdutoRedis redisItem = ProdutoMapper.toRedis(
                        p,
                        p.getVendedor() != null ? p.getVendedor().getId() : null
                );
                produtoRedisRepository.save(redisItem);
                count++;
            }
        }
        return count;
    }
}