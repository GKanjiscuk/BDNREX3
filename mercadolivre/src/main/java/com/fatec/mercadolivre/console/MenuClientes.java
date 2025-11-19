package com.fatec.mercadolivre.console;

import com.fatec.mercadolivre.model.entidades.Cliente;
import com.fatec.mercadolivre.model.entidades.Endereco;
import com.fatec.mercadolivre.model.entidades.Produto;
import com.fatec.mercadolivre.service.ClienteService;
import com.fatec.mercadolivre.service.ProdutoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

@Component
public class MenuClientes {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ProdutoService produtoService;

    private final Scanner scanner = new Scanner(System.in);

    public void exibirMenu(Object usuarioAtual) {
        while (true) {
            System.out.println("\n--- Menu de Clientes ---");
            System.out.println("1. Cadastrar novo Cliente");
            System.out.println("2. Ler dados de um Cliente");
            System.out.println("3. Atualizar um Cliente");
            System.out.println("4. Deletar um Cliente");
            System.out.println("5. Gerenciar Produtos Favoritos");
            System.out.println("6. Listar todos os Clientes");
            System.out.println("7. Voltar ao Menu Principal");

            String escolha = scanner.nextLine();
            switch (escolha) {
                case "1": criarCliente(); break;
                case "2": lerClienteDetalhado(); break;
                case "3": atualizarCliente(); break;
                case "4": deletarCliente(); break;
                case "5": menuFavoritos(); break;
                case "6": listarTodosClientes(); break;
                case "7": return;
                default: System.out.println("Opção inválida.");
            }
        }
    }


    private void criarCliente() {
        System.out.println("\n-- Cadastro de Novos Clientes --");
        Cliente c = new Cliente();
        System.out.print("Nome completo: "); c.setNome(scanner.nextLine());
        System.out.print("CPF: "); c.setCpf(scanner.nextLine());
        System.out.print("E-mail: "); c.setEmail(scanner.nextLine());

        List<Endereco> enderecos = coletarEnderecos();
        try {
            Cliente novo = clienteService.criarCliente(c, enderecos);
            System.out.println("Cliente criado com sucesso! ID: " + novo.getId());
        } catch (Exception e) {
            System.out.println("Erro ao criar cliente: " + e.getMessage());
        }
    }

    private List<Endereco> coletarEnderecos() {
        return new ArrayList<>();
    }

    private void lerClienteDetalhado() {
        String id = selecionarClienteId();
        if (id == null) return;

        Optional<Cliente> clienteOpt = clienteService.buscarPorId(id);
        if (clienteOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();
            System.out.println("\n--- Dados Completos do Cliente ---");
            System.out.println("ID: " + cliente.getId());
            System.out.println("Nome: " + cliente.getNome());
            System.out.println("E-mail: " + cliente.getEmail());

            System.out.println("\n--- Produtos Favoritos ---");
            if (cliente.getFavoritos() != null && !cliente.getFavoritos().isEmpty()) {
                cliente.getFavoritos().forEach(produto -> {
                    System.out.println("  - " + produto.getNome() + " (ID: " + produto.getId() + ")");
                });
            } else {
                System.out.println("Nenhum produto favorito cadastrado.");
            }
        } else {
            System.out.println("Cliente não encontrado.");
        }
    }

    private void atualizarCliente() {
        String id = selecionarClienteId();
        if (id == null) return;

        System.out.print("Novo e-mail: ");
        Cliente novosDados = new Cliente();
        novosDados.setEmail(scanner.nextLine());

        Cliente atualizado = clienteService.atualizarCliente(id, novosDados);
        if (atualizado != null) {
            System.out.println("Cliente atualizado com sucesso.");
        } else {
            System.out.println("Falha ao atualizar cliente ou cliente não encontrado.");
        }
    }

    private void deletarCliente() {
        String id = selecionarClienteId();
        if (id == null) return;

        if (clienteService.deletarCliente(id)) {
            System.out.println("Cliente deletado com sucesso.");
        } else {
            System.out.println("Falha ao deletar cliente ou cliente não encontrado.");
        }
    }

    private void listarTodosClientes() {
        System.out.println("--- Lista de Clientes ---");
        clienteService.listarTodos().forEach(c ->
                System.out.println("ID: " + c.getId() + ", Nome: " + c.getNome())
        );
    }

    public String selecionarClienteId() {
        List<Cliente> clientes = clienteService.listarTodos();
        if (clientes.isEmpty()) {
            System.out.println("\nNenhum cliente cadastrado.");
            return null;
        }

        System.out.println("\n--- Selecione o Cliente ---");
        for (int i = 0; i < clientes.size(); i++) {
            System.out.printf("%d. Nome: %s, ID: %s\n", (i + 1), clientes.get(i).getNome(), clientes.get(i).getId());
        }

        System.out.print("Digite o número do cliente (ou '0' para cancelar): ");
        String escolha = scanner.nextLine();

        if (escolha.equals("0")) {
            return null;
        }

        try {
            int indice = Integer.parseInt(escolha) - 1;
            if (indice >= 0 && indice < clientes.size()) {
                return clientes.get(indice).getId();
            } else {
                System.out.println("Opção inválida.");
                return null;
            }
        } catch (NumberFormatException e) {
            System.out.println("Opção inválida. Digite um número.");
            return null;
        }
    }


    private void menuFavoritos() {
        while (true) {
            System.out.println("\n--- Gerenciar Favoritos ---");
            System.out.println("1. Adicionar Produto Favorito");
            System.out.println("2. Remover Produto Favorito");
            System.out.println("3. Voltar ao Menu de Clientes");

            String escolha = scanner.nextLine();
            if (escolha.equals("1")) {
                gerenciarFavorito(true);
            } else if (escolha.equals("2")) {
                gerenciarFavorito(false);
            } else if (escolha.equals("3")) {
                return;
            } else {
                System.out.println("Opção inválida.");
            }
        }
    }

    private void gerenciarFavorito(boolean adicionar) {
        String clienteId = selecionarClienteId();
        if (clienteId == null) return;

        String produtoId = null;

        if (produtoId == null) return;

        if (adicionar) {
            if (clienteService.adicionarFavorito(clienteId, produtoId)) {
                System.out.println("Produto adicionado aos favoritos com sucesso.");
            } else {
                System.out.println("Falha ao adicionar favorito (talvez já exista).");
            }
        } else {
            if (clienteService.removerFavorito(clienteId, produtoId)) {
                System.out.println("Produto removido dos favoritos com sucesso.");
            } else {
                System.out.println("Falha ao remover favorito (talvez não exista).");
            }
        }
    }
}