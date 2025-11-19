package com.fatec.mercadolivre.console;

import com.fatec.mercadolivre.model.entidades.Administrador;
import com.fatec.mercadolivre.model.entidades.Cliente;
import com.fatec.mercadolivre.model.entidades.Vendedor;
import com.fatec.mercadolivre.service.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Scanner;

@Component
public class MenuPrincipal implements CommandLineRunner {

    @Autowired private MenuClientes menuClientes;
    @Autowired private MenuVendedores menuVendedores;
    @Autowired private MenuProdutos menuProdutos;
    @Autowired private MenuCompras menuCompras;

    @Autowired private DatabaseSeeder seederManager;
    @Autowired private MenuMigracao menuMigracao;
    @Autowired private SecurityService securityService;

    private final Scanner scanner = new Scanner(System.in);
    private boolean estaLogado = false;
    private Object usuarioAtual = null;

    // Timeout config
    private static final long TEMPO_LIMITE_MS = 120000; // 2 minutos
    private long ultimaInteracao;

    @Override
    public void run(String... args) throws Exception {
        while (true) {
            executarLoginInicial();

            if (estaLogado) {
                ultimaInteracao = System.currentTimeMillis();
                menuPrincipal();
            }
            // Se sair do menu (logout ou timeout), o loop reinicia e pede login de novo
        }
    }

    private void executarLoginInicial() {
        System.out.println("\n--- BEM-VINDO(A) AO MERCADOLIVRE BDNR (EX3) ---");
        System.out.println("Por favor, faça login para continuar.");
        System.out.println("(Dica: Se for a 1ª vez, digite 'setup' no e-mail para criar os usuários)");

        System.out.print("E-mail: ");
        String email = scanner.nextLine();

        // --- SOLUÇÃO DO PROBLEMA "OVO E GALINHA" ---
        if (email.equalsIgnoreCase("setup")) {
            System.out.println(">> Executando configuração inicial do banco de dados...");
            seederManager.popularBancoDeDados();
            System.out.println(">> Banco populado! Tente logar com: admin@fatec.com / 123456");
            return; // Retorna para o loop e pede login novamente
        }
        // -------------------------------------------

        System.out.print("Senha: ");
        String senha = scanner.nextLine();

        Optional<Object> usuarioAutenticado = securityService.autenticar(email, senha);

        if (usuarioAutenticado.isPresent()) {
            estaLogado = true;
            usuarioAtual = usuarioAutenticado.get();

            String nome = "";
            String tipo = "ADMIN/N/A";

            if (usuarioAtual instanceof Cliente cliente) {
                nome = cliente.getNome();
                tipo = "Cliente";
            } else if (usuarioAtual instanceof Vendedor vendedor) {
                nome = vendedor.getNome();
                tipo = "Vendedor";
            } else if (usuarioAtual instanceof Administrador administrador) {
                nome = administrador.getNome();
                tipo = "Administrador";
            }

            System.out.printf("✅ Login bem-sucedido! Bem-vindo(a), %s (%s).\n", nome, tipo);
        } else {
            System.out.println("❌ E-mail ou senha inválidos.");
        }
    }

    private void menuPrincipal() {
        String nomeUsuario = "";
        String tipoUsuario = "N/A";

        if (usuarioAtual instanceof Cliente cliente) {
            nomeUsuario = cliente.getNome();
            tipoUsuario = "Cliente";
        } else if (usuarioAtual instanceof Vendedor vendedor) {
            nomeUsuario = vendedor.getNome();
            tipoUsuario = "Vendedor";
        } else if (usuarioAtual instanceof Administrador administrador) {
            nomeUsuario = administrador.getNome();
            tipoUsuario = "Administrador";
        }

        while (estaLogado) {
            // Timeout Check
            if (System.currentTimeMillis() - ultimaInteracao > TEMPO_LIMITE_MS) {
                System.out.println("\n⏳ SESSÃO EXPIRADA POR INATIVIDADE. FAÇA LOGIN NOVAMENTE.");
                estaLogado = false;
                usuarioAtual = null;
                return;
            }

            System.out.printf("\n\n--- Menu Principal (Usuário: %s | Tipo: %s) ---\n", nomeUsuario, tipoUsuario);
            System.out.println("1. Gerenciar Clientes");
            System.out.println("2. Gerenciar Vendedores");
            System.out.println("3. Gerenciar Produtos (Cache/EX3)");
            System.out.println("4. Gerenciar Compras");
            System.out.println("5. POPULAR BANCO DE DADOS PARA TESTES");
            System.out.println("6. ⚡ EX3: GERENCIAR SINCRONIZAÇÃO/MIGRAÇÃO");
            System.out.println("7. Sair (Logout)");

            String escolha = scanner.nextLine();
            ultimaInteracao = System.currentTimeMillis(); // Atualiza tempo

            switch (escolha) {
                case "1": menuClientes.exibirMenu(usuarioAtual); break;
                case "2": menuVendedores.exibirMenu(usuarioAtual); break;
                case "3": menuProdutos.exibirMenu(usuarioAtual); break;
                case "4": menuCompras.exibirMenu(usuarioAtual); break;
                case "5":
                    seederManager.popularBancoDeDados();
                    break;
                case "6":
                    if (tipoUsuario.equals("Administrador")) {
                        menuMigracao.exibirMenu(usuarioAtual);
                    } else {
                        System.out.println("⚠️ ACESSO NEGADO: Apenas administradores podem sincronizar dados.");
                    }
                    break;
                case "7":
                    System.out.println("Saindo do programa...");
                    estaLogado = false;
                    usuarioAtual = null;
                    return;
                default:
                    System.out.println("Opção inválida.");
            }
        }
    }
}