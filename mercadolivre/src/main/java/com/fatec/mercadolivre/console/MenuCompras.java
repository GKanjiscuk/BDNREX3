package com.fatec.mercadolivre.console;

import com.fatec.mercadolivre.model.entidades.Compra;
import com.fatec.mercadolivre.model.entidades.ItemCompra;
import com.fatec.mercadolivre.model.entidades.Produto;
import com.fatec.mercadolivre.service.CompraService;
import com.fatec.mercadolivre.service.ProdutoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

@Component
public class MenuCompras {

    @Autowired private CompraService compraService;
    @Autowired private ProdutoService produtoService;

    @Autowired private MenuClientes menuClientes;
    @Autowired private MenuProdutos menuProdutos;

    private final Scanner scanner = new Scanner(System.in);

    public void exibirMenu(Object usuarioAtual) {
        while (true) {
            System.out.println("\n--- Menu de Compras ---");
            System.out.println("1. Registrar nova Compra");
            System.out.println("2. Ler dados de uma Compra");
            System.out.println("3. Listar compras de um Cliente");
            System.out.println("4. Listar todas as Compras");
            System.out.println("5. Voltar ao Menu Principal");

            String escolha = scanner.nextLine();
            switch (escolha) {
                case "1": criarCompra(); break;
                case "2": lerCompra(); break;
                case "3": listarComprasCliente(); break;
                case "4": listarTodasCompras(); break;
                case "5": return;
                default: System.out.println("Opção inválida.");
            }
        }
    }

    /**
     * Substitui a lógica de novaCompra.py
     */
    private void criarCompra() {
        String clienteId = menuClientes.selecionarClienteId();
        if (clienteId == null) {
            System.out.println("Criação de compra cancelada: Cliente não selecionado.");
            return;
        }

        List<ItemCompra> itens = new ArrayList<>();
        System.out.println("\n--- Adicionar Produtos à Compra ---");

        while (true) {
            String produtoId = menuProdutos.selecionarProdutoId();

            if (produtoId == null) {
                System.out.println("Finalizando seleção de produtos.");
                break;
            }

            Optional<Produto> produtoOpt = produtoService.buscarPorIdMongo(produtoId);
            if (produtoOpt.isEmpty()) {
                System.out.println("Produto não encontrado. Tente novamente.");
                continue;
            }
            Produto p = produtoOpt.get();

            System.out.printf("Quantidade de '%s': ", p.getNome());
            try {
                int quantidade = Integer.parseInt(scanner.nextLine());
                if (quantidade <= 0) throw new NumberFormatException();

                ItemCompra item = new ItemCompra();
                item.setProdutoId(p.getId());
                item.setNomeProduto(p.getNome());
                item.setPrecoUnitario(p.getPreco());
                item.setQuantidade(quantidade);
                item.setVendedorId(p.getVendedor() != null ? p.getVendedor().getId() : "N/A");

                itens.add(item);
                System.out.printf("Produto adicionado. Subtotal: R$%.2f\n", p.getPreco() * quantidade);

            } catch (NumberFormatException e) {
                System.out.println("Quantidade inválida. Por favor, digite um número inteiro positivo.");
            }
        }

        if (itens.isEmpty()) {
            System.out.println("\nCompra cancelada. Nenhum produto adicionado.");
            return;
        }

        try {
            Compra nova = compraService.registrarNovaCompra(clienteId, itens);
            System.out.printf("\nCompra registrada com sucesso! ID:%s, Total: R$%.2f\n", nova.getId(), nova.getTotal());
        } catch (IllegalArgumentException e) {
            System.out.println("Erro ao registrar compra: " + e.getMessage());
        }
    }

    /**
     * Substitui a lógica de lerCompra.py
     */
    private void lerCompra() {
        String compraId = selecionarCompraId();
        if (compraId == null) return;

        Optional<Compra> compraOpt = compraService.buscarPorId(compraId);
        if (compraOpt.isPresent()) {
            Compra compra = compraOpt.get();
            System.out.println("\n--- Dados da Compra ---");
            System.out.printf("ID da Compra: %s\n", compra.getId());
            System.out.printf("Cliente: %s\n", compra.getCliente() != null ? compra.getCliente().getNome() : "N/A");
            System.out.printf("Data da Compra: %s\n", compra.getDataCompra());
            System.out.printf("Total da Compra: R$%.2f\n", compra.getTotal());
            System.out.println("\n--- Itens da Compra ---");
            compra.getProdutos().forEach(item ->
                    System.out.printf("  - Produto: %s (Quant: %d, Preço Unit: R$%.2f)\n",
                            item.getNomeProduto(), item.getQuantidade(), item.getPrecoUnitario())
            );
        } else {
            System.out.println("Compra não encontrada.");
        }
    }

    /**
     * Substitui o selecionarCompra do Python
     */
    private String selecionarCompraId() {
        List<Compra> compras = compraService.listarTodas();
        if (compras.isEmpty()) {
            System.out.println("\nNenhuma compra registrada.");
            return null;
        }

        System.out.println("\n--- Selecione a Compra ---");
        for (int i = 0; i < compras.size(); i++) {
            Compra c = compras.get(i);
            String clienteNome = c.getCliente() != null ? c.getCliente().getNome() : "N/A";
            System.out.printf("%d. Cliente: %s, Total: R$%.2f, ID: %s\n",
                    (i + 1), clienteNome, c.getTotal(), c.getId());
        }

        System.out.print("Digite o número da compra (ou '0' para cancelar): ");
        String escolha = scanner.nextLine();

        if (escolha.equals("0")) {
            return null;
        }

        try {
            int indice = Integer.parseInt(escolha) - 1;
            if (indice >= 0 && indice < compras.size()) {
                return compras.get(indice).getId();
            } else {
                System.out.println("Opção inválida.");
                return null;
            }
        } catch (NumberFormatException e) {
            System.out.println("Opção inválida. Digite um número.");
            return null;
        }
    }

    /**
     * Substitui o listarComprasCliente do Python
     */
    private void listarComprasCliente() {
        String clienteId = menuClientes.selecionarClienteId();
        if (clienteId == null) return;

        List<Compra> compras = compraService.listarComprasPorCliente(clienteId);

        if (compras.isEmpty()) {
            System.out.println("Nenhuma compra encontrada para este cliente.");
            return;
        }

        System.out.println("--- Compras do Cliente ---");
        compras.forEach(compra -> {
            System.out.printf("ID Compra: %s | Total: R$%.2f | Data: %s\n",
                    compra.getId(), compra.getTotal(), compra.getDataCompra());
            compra.getProdutos().forEach(item ->
                    System.out.printf("  - Produto: %s (%d unidades)\n", item.getNomeProduto(), item.getQuantidade())
            );
        });
    }

    /**
     * Substitui o listarTodasCompras do Python
     */
    private void listarTodasCompras() {
        System.out.println("--- Todas as Compras ---");
        List<Compra> compras = compraService.listarTodas();

        if (compras.isEmpty()) {
            System.out.println("Nenhuma compra encontrada.");
            return;
        }

        compras.forEach(compra -> {
            String clienteNome = compra.getCliente() != null ? compra.getCliente().getNome() : "N/A";
            System.out.printf("\nID: %s | Cliente: %s | Total: R$%.2f\n",
                    compra.getId(), clienteNome, compra.getTotal());
        });
    }
}