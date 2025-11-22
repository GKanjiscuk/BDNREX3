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
            System.out.println("1. 📤 (Passo A) Migrar Produtos do MongoDB para o REDIS");
            System.out.println("2. ✏️ (Passo B) Manipular Produtos no REDIS (Via Menu Produtos)");
            System.out.println("3. 🔄 (Passo C) Sincronizar REDIS -> MONGODB (Commitar)");
            System.out.println("4. 🔙 Voltar ao Menu Principal");

            System.out.print("Escolha: ");
            String escolha = scanner.nextLine();

            switch (escolha) {
                case "1":
                    migrarMongoParaRedis();
                    break;
                case "2":
                    System.out.println(">> Abrindo Menu de Produtos (Itens editados estão em Cache!)...");
                    menuProdutos.exibirMenu(usuarioAtual);
                    break;
                case "3":
                    sincronizarRedisParaMongo();
                    break;
                case "4":
                    return;
                default:
                    System.out.println("Opção inválida.");
            }
        }
    }

    private void migrarMongoParaRedis() {
        System.out.println("\n--- Selecione Produtos para Migrar para o REDIS ---");

        List<Produto> produtosMongo = produtoService.listarTodosMongo();
        if (produtosMongo.isEmpty()) {
            System.out.println("Nenhum produto encontrado no MongoDB para migração.");
            return;
        }

        List<String> idsParaMigrar = new ArrayList<>();
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