package com.fatec.mercadolivre.console;

import com.fatec.mercadolivre.model.entidades.Administrador;
import com.fatec.mercadolivre.model.entidades.Cliente;
import com.fatec.mercadolivre.model.entidades.Vendedor;
import com.fatec.mercadolivre.service.VendedorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Scanner;

@Component
public class MenuVendedores {

    @Autowired
    private VendedorService vendedorService;

    private final Scanner scanner = new Scanner(System.in);

    public void exibirMenu(Object usuarioAtual) {
        // Verifica permissão de acesso ao menu
        if (!(usuarioAtual instanceof Vendedor || usuarioAtual instanceof Administrador)) {
            System.out.println("\n⚠️ ACESSO NEGADO: Apenas Vendedores e Administradores podem gerenciar esta seção.");
            return;
        }

        while (true) {
            System.out.println("\n--- Menu de Vendedores ---");
            System.out.println("1. Cadastrar novo Vendedor");
            System.out.println("2. Ler dados de um Vendedor");
            System.out.println("3. Atualizar um Vendedor");
            System.out.println("4. Deletar um Vendedor");
            System.out.println("5. Listar todos os Vendedores");
            System.out.println("6. Voltar ao Menu Principal");

            String escolha = scanner.nextLine();
            switch (escolha) {
                case "1":
                    // Apenas Admin deveria criar vendedores, mas se quiser deixar aberto:
                    criarVendedor();
                    break;
                case "2":
                    lerVendedor();
                    break;
                case "3":
                    // Lógica de Atualização com Permissão
                    if (usuarioAtual instanceof Cliente) {
                        System.out.println("⚠️ ACESSO NEGADO.");
                    } else if (usuarioAtual instanceof Vendedor vendedorLogado) {
                        // Vendedor: Passa o ID dele mesmo
                        atualizarVendedor(vendedorLogado.getId());
                    } else {
                        // Administrador: Passa null para que o método pergunte qual vendedor atualizar
                        atualizarVendedor(null);
                    }
                    break;
                case "4":
                    deletarVendedor();
                    break;
                case "5":
                    listarTodosVendedores();
                    break;
                case "6":
                    return;
                default:
                    System.out.println("Opção inválida.");
            }
        }
    }

    private void criarVendedor() {
        System.out.println("\n-- Cadastro de novo vendedor --");
        Vendedor v = new Vendedor();
        System.out.print("Nome completo: "); v.setNome(scanner.nextLine());
        System.out.print("CNPJ: "); v.setCnpj(scanner.nextLine());
        System.out.print("E-mail: "); v.setEmail(scanner.nextLine());
        // Senha será tratada no Service
        System.out.print("Senha: "); v.setSenha(scanner.nextLine());

        try {
            vendedorService.criarVendedor(v);
            System.out.println("Vendedor criado com sucesso!");
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void lerVendedor() {
        String id = selecionarVendedorId();
        if (id == null) return;

        Optional<Vendedor> vendedorOpt = vendedorService.buscarPorId(id);
        if (vendedorOpt.isPresent()) {
            System.out.println("Dados do Vendedor: " + vendedorOpt.get().toString());
        } else {
            System.out.println("Vendedor não encontrado.");
        }
    }

    // --- A CORREÇÃO ESTÁ AQUI EMBAIXO ---
    // Alteramos para aceitar um parâmetro (String idPreSelecionado)
    private void atualizarVendedor(String idPreSelecionado) {
        String id;

        // Se já veio um ID (do Vendedor logado), usa ele.
        // Se veio null (do Admin), pede para selecionar na lista.
        if (idPreSelecionado != null) {
            id = idPreSelecionado;
        } else {
            id = selecionarVendedorId();
        }

        if (id == null) return; // Cancelado

        System.out.print("Novo e-mail: ");
        Vendedor novosDados = new Vendedor();
        novosDados.setEmail(scanner.nextLine());

        Vendedor atualizado = vendedorService.atualizarVendedor(id, novosDados);
        if (atualizado != null) {
            System.out.println("Vendedor atualizado com sucesso.");
        } else {
            System.out.println("Falha ao atualizar vendedor.");
        }
    }

    private void deletarVendedor() {
        String id = selecionarVendedorId();
        if (id == null) return;

        if (vendedorService.deletarVendedor(id)) {
            System.out.println("Vendedor deletado com sucesso.");
        } else {
            System.out.println("Falha ao deletar vendedor.");
        }
    }

    private void listarTodosVendedores() {
        System.out.println("--- Lista de Vendedores ---");
        vendedorService.listarTodos().forEach(v ->
                System.out.println("ID: " + v.getId() + ", Nome: " + v.getNome() + ", Loja: " + v.getNomeLoja())
        );
    }

    public String selecionarVendedorId() {
        System.out.println("\n--- Selecione um Vendedor ---");
        vendedorService.listarTodos().forEach(v ->
                System.out.printf("ID: %s, Nome: %s\n", v.getId(), v.getNome())
        );
        System.out.print("Digite o ID do vendedor (ou '0' para cancelar): ");
        String id = scanner.nextLine();
        return id.equals("0") ? null : id;
    }
}