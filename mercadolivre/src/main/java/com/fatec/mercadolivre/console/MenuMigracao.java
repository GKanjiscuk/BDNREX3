package com.fatec.mercadolivre.console;

import com.fatec.mercadolivre.model.entidades.Produto;
import com.fatec.mercadolivre.service.ProdutoService;
import com.fatec.mercadolivre.service.SincronizacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Component
public class MenuMigracao {

    @Autowired private SincronizacaoService sincronizacaoService;
    @Autowired private ProdutoService produtoService;
    @Autowired private MenuProdutos menuProdutos;

    private final Scanner scanner = new Scanner(System.in);

    public void exibirMenu(Object usuarioAtual) {
        while (true) {
            System.out.println("\n--- Menu de Sincronização (EX3) ---");
            System.out.println("1. 📤 Migrar Produtos do MongoDB para o REDIS (Cache)");
            System.out.println("2. 🔄 Sincronizar REDIS -> MONGODB (Commitar Mudanças)");
            System.out.println("3. 🔙 Voltar ao Menu Principal");

            String escolha = scanner.nextLine();

            switch (escolha) {
                case "1": migrarMongoParaRedis(); break;
                case "2": sincronizarRedisParaMongo(); break;
                case "3": return;
                default: System.out.println("Opção inválida.");
            }
        }
    }

    /**
     * Implementa a lógica 'Retirar do MongoDB e colocar no Redis' [cite: 10]
     */
    private void migrarMongoParaRedis() {
        System.out.println("\n--- Selecione Produtos para Migrar para o REDIS ---");

        List<Produto> produtosMongo = produtoService.listarTodosMongo(); // Lista do MongoDB
        if (produtosMongo.isEmpty()) {
            System.out.println("Nenhum produto encontrado no MongoDB para migração.");
            return;
        }

        List<String> idsParaMigrar = new ArrayList<>();
        // Lógica simples: migrar os 3 primeiros itens ou um item novo (EX3 )
        int maxItens = Math.min(3, produtosMongo.size());

        System.out.printf("Migrando os primeiros %d produtos encontrados para manipulação no cache...\n", maxItens);
        for (int i = 0; i < maxItens; i++) {
            idsParaMigrar.add(produtosMongo.get(i).getId());
            System.out.printf("  - Adicionado: %s\n", produtosMongo.get(i).getNome());
        }

        int count = sincronizacaoService.migrarProdutosMongoParaRedis(idsParaMigrar);
        System.out.printf("✅ Migração concluída. %d produtos movidos para o cache REDIS. Eles agora são manipuláveis via Menu Produtos.\n", count);
    }

    /**
     * Implementa a lógica 'Devolver os itens para o MongoDB' [cite: 12]
     */
    private void sincronizarRedisParaMongo() {
        System.out.println("\n--- Iniciando Sincronização e Commit ---");
        int count = sincronizacaoService.sincronizarRedisParaMongo();
        System.out.printf("✅ Sincronização concluída. %d produtos atualizados/inseridos no MongoDB e cache REDIS limpo.\n", count);
    }
}