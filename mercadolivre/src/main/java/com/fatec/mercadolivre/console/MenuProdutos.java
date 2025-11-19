package com.fatec.mercadolivre.console;

import com.fatec.mercadolivre.model.entidades.Administrador;
import com.fatec.mercadolivre.model.entidades.Cliente;
import com.fatec.mercadolivre.model.entidades.Produto;
import com.fatec.mercadolivre.model.entidades.Vendedor;
import com.fatec.mercadolivre.model.entidades.redis.ProdutoRedis;
import com.fatec.mercadolivre.service.ProdutoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

@Component
public class MenuProdutos {

    @Autowired private ProdutoService produtoService;
    @Autowired private MenuVendedores menuVendedores;

    private final Scanner scanner = new Scanner(System.in);

    public void exibirMenu(Object usuarioAtual) {
        while (true) {
            System.out.println("\n--- Menu de Produtos (MODO REDIS-FIRST) ---");
            System.out.println("1. Cadastrar novo Produto");
            System.out.println("2. Ler dados de um Produto");
            System.out.println("3. Atualizar um Produto");
            System.out.println("4. Deletar um Produto");
            System.out.println("5. Listar todos os Produtos");
            System.out.println("6. Buscar produtos de um Vendedor");
            System.out.println("7. Voltar ao Menu Principal");

            String escolha = scanner.nextLine();
            switch (escolha) {
                case "1": criarProduto(usuarioAtual); break;
                case "2": lerProduto(); break;
                case "3": atualizarProduto(usuarioAtual); break;
                case "4": deletarProduto(usuarioAtual); break;
                case "5": listarTodosProdutos(); break;
                case "6": buscarProdutosVendedor(); break;
                case "7": return;
                default: System.out.println("Opção inválida.");
            }
        }
    }

    private void criarProduto(Object usuarioAtual) {
        if (usuarioAtual instanceof Cliente) {
            System.out.println("⛔ ACESSO NEGADO: Clientes não podem cadastrar produtos.");
            return;
        }

        String vendedorId = null;

        if (usuarioAtual instanceof Vendedor) {
            vendedorId = ((Vendedor) usuarioAtual).getId();
            System.out.println("Cadastrando produto para: " + ((Vendedor) usuarioAtual).getNome());
        }
        else if (usuarioAtual instanceof Administrador) {
            vendedorId = menuVendedores.selecionarVendedorId();
        }

        if (vendedorId == null) {
            System.out.println("Operação cancelada.");
            return;
        }

        System.out.println("\n--- Cadastro de Novo Produto (REDIS) ---");
        Produto p = new Produto();
        System.out.print("Nome do produto: "); p.setNome(scanner.nextLine());
        System.out.print("Descrição: "); p.setDescricao(scanner.nextLine());

        try {
            System.out.print("Preço: "); p.setPreco(Double.parseDouble(scanner.nextLine()));
            produtoService.criarProduto(p, vendedorId);
            System.out.println("Produto criado com sucesso! (Salvo no Redis)");
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void lerProduto() {
        String produtoId = selecionarProdutoId();
        if (produtoId == null) return;

        Optional<ProdutoRedis> produtoOpt = produtoService.buscarPorIdRedis(produtoId);

        if (produtoOpt.isPresent()) {
            ProdutoRedis p = produtoOpt.get();
            System.out.println("\n--- Dados Detalhados do Produto (REDIS) ---");
            System.out.printf("ID do Produto: %s\n", p.getId());
            System.out.printf("Nome: %s\n", p.getNome());
            System.out.printf("Descrição: %s\n", p.getDescricao());
            System.out.printf("Preço: R$%.2f\n", p.getPreco());
            System.out.printf("Vendedor ID: %s\n", p.getVendedorId());
        } else {
            System.out.println("Produto não encontrado no cache Redis.");
        }
    }

    private void atualizarProduto(Object usuarioAtual) {
        if (usuarioAtual instanceof Cliente) {
            System.out.println("⛔ ACESSO NEGADO.");
            return;
        }

        System.out.print("Digite o ID do produto para atualizar: ");
        String produtoId = scanner.nextLine();

        if (usuarioAtual instanceof Vendedor) {
            Optional<ProdutoRedis> p = produtoService.buscarPorIdRedis(produtoId);
            if (p.isPresent() && !p.get().getVendedorId().equals(((Vendedor) usuarioAtual).getId())) {
                System.out.println("⛔ VOCÊ SÓ PODE ALTERAR SEUS PRÓPRIOS PRODUTOS.");
                return;
            }
        }

        try {
            System.out.print("Novo preço: ");
            Double novoPreco = Double.parseDouble(scanner.nextLine());
            Produto novosDados = new Produto();
            novosDados.setPreco(novoPreco);

            if (produtoService.atualizarProdutoRedis(produtoId, novosDados) != null) {
                System.out.println("Produto atualizado no REDIS.");
            } else {
                System.out.println("Falha ao atualizar.");
            }
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void deletarProduto(Object usuarioAtual) {
        if (usuarioAtual instanceof Cliente) {
            System.out.println("⛔ ACESSO NEGADO.");
            return;
        }

        System.out.print("Digite o ID do produto para deletar: ");
        String produtoId = scanner.nextLine();

        if (usuarioAtual instanceof Vendedor) {
            Optional<ProdutoRedis> p = produtoService.buscarPorIdRedis(produtoId);
            if (p.isPresent() && !p.get().getVendedorId().equals(((Vendedor) usuarioAtual).getId())) {
                System.out.println("⛔ VOCÊ SÓ PODE DELETAR SEUS PRÓPRIOS PRODUTOS.");
                return;
            }
        }

        if (produtoService.deletarProdutoRedis(produtoId)) {
            System.out.println("Produto deletado do REDIS.");
        } else {
            System.out.println("Falha ao deletar.");
        }
    }

    private void listarTodosProdutos() {
        System.out.println("--- Lista de Produtos (DO CACHE REDIS) ---");
        produtoService.listarTodosRedis().forEach(p -> {
            System.out.printf("ID: %s, Nome: %s, Preço: R$%.2f, Vendedor ID: %s\n",
                    p.getId(), p.getNome(), p.getPreco(), p.getVendedorId());
        });
    }

    private void buscarProdutosVendedor() {
        System.out.print("Digite o ID do vendedor para buscar seus produtos no REDIS: ");
        String vendedorId = scanner.nextLine();

        List<ProdutoRedis> produtos = produtoService.findByVendedorIdRedis(vendedorId);

        if (produtos.isEmpty()) {
            System.out.println("Nenhum produto encontrado para este vendedor no cache Redis.");
            return;
        }

        System.out.println("--- Produtos do Vendedor (REDIS) ---");
        produtos.forEach(p -> {
            System.out.printf("ID: %s, Nome: %s, Preço: R$%.2f\n", p.getId(), p.getNome(), p.getPreco());
        });
    }

    public String selecionarProdutoId() {
        List<ProdutoRedis> produtos = produtoService.listarTodosRedis();
        if (produtos.isEmpty()) {
            System.out.println("\nNenhum produto cadastrado no Redis.");
            return null;
        }

        System.out.println("\n--- Selecione um Produto (REDIS) ---");
        for (int i = 0; i < produtos.size(); i++) {
            ProdutoRedis p = produtos.get(i);
            System.out.printf("%d. Nome: %s, Preço: R$%.2f, ID: %s\n",
                    (i + 1), p.getNome(), p.getPreco(), p.getId());
        }

        System.out.print("Digite o número do produto (ou '0' para cancelar): ");
        String escolha = scanner.nextLine();

        if (escolha.equals("0")) {
            return null;
        }

        try {
            int indice = Integer.parseInt(escolha) - 1;
            if (indice >= 0 && indice < produtos.size()) {
                return produtos.get(indice).getId();
            } else {
                System.out.println("Opção inválida.");
                return null;
            }
        } catch (NumberFormatException e) {
            System.out.println("Opção inválida. Digite um número.");
            return null;
        }
    }
}