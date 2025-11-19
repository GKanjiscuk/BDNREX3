package com.fatec.mercadolivre.console;

import com.fatec.mercadolivre.model.entidades.*;
import com.fatec.mercadolivre.model.entidades.redis.ProdutoRedis;
import com.fatec.mercadolivre.repository.*;
import com.fatec.mercadolivre.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class DatabaseSeeder {

    @Autowired private ClienteService clienteService;
    @Autowired private VendedorService vendedorService;
    @Autowired private ProdutoService produtoService;
    @Autowired private CompraService compraService;
    @Autowired private SecurityService securityService;

    @Autowired private ClienteRepository clienteRepository;
    @Autowired private VendedorRepository vendedorRepository;
    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private CompraRepository compraRepository;
    @Autowired private AdministradorRepository administradorRepository;

    public void popularBancoDeDados() {
        System.out.println("---------------------------------------------");
        System.out.println("⚙️  INICIANDO POPULAÇÃO MANUAL DE DADOS...");
        System.out.println("---------------------------------------------");

        limparBancoDeDados();

        criarAdmin("Admin Global", "admin@fatec.com", "123456");
        System.out.println("✓ Usuário Admin criado (admin@fatec.com | 123456).");

        Vendedor v1 = criarVendedor("João da Silva", "001.111.222-33", "ShopTech", "11.111.111/0001-11", "vendedor@teste.com", "123456");
        Vendedor v2 = criarVendedor("Maria Oliveira", "444.555.666-77", "CasaDecor", "22.222.222/0001-22", "maria@teste.com", "senha123");

        Cliente c1 = criarCliente("Vinicius Santos", "999.888.777-66", "vinicius@gmail.com", "123456");
        Cliente c2 = criarCliente("Ana Souza", "123.456.789-00", "ana.souza@outlook.com", "senha123");

        Produto p1 = criarProduto("Notebook Gamer X", "Alta performance para jogos.", 5500.00, v1.getId());
        Produto p2 = criarProduto("Mouse Sem Fio", "Ergonômico e preciso.", 120.00, v1.getId());
        Produto p3 = criarProduto("Conjunto de Taças", "Vidro cristalino, 6 unidades.", 89.90, v2.getId());
        Produto p4 = criarProduto("Luminária LED", "Com ajuste de cor.", 210.50, v2.getId());

        if (p1 != null && p3 != null) {
            clienteService.adicionarFavorito(c1.getId(), p1.getId());
            clienteService.adicionarFavorito(c1.getId(), p3.getId());
            System.out.println("✓ Favoritos adicionados ao Cliente 1.");
        }

        if (p2 != null && p4 != null) {
            List<ItemCompra> itensCompra = new ArrayList<>();

            ItemCompra item1 = new ItemCompra();
            item1.setProdutoId(p2.getId());
            item1.setNomeProduto(p2.getNome());
            item1.setPrecoUnitario(p2.getPreco());
            item1.setQuantidade(2);
            item1.setVendedorId(v1.getId());
            itensCompra.add(item1);

            ItemCompra item2 = new ItemCompra();
            item2.setProdutoId(p4.getId());
            item2.setNomeProduto(p4.getNome());
            item2.setPrecoUnitario(p4.getPreco());
            item2.setQuantidade(1);
            item2.setVendedorId(v2.getId());
            itensCompra.add(item2);

            compraService.registrarNovaCompra(c1.getId(), itensCompra);
            System.out.println("✓ Compra registrada para o Cliente 1.");
        }

        System.out.println("---------------------------------------------");
        System.out.println("✅ POPULAÇÃO CONCLUÍDA. Total de itens: ");
        System.out.println("   Clientes: " + clienteService.listarTodos().size());
        System.out.println("   Vendedores: " + vendedorService.listarTodos().size());
        System.out.println("   Produtos (Redis): " + produtoService.listarTodosRedis().size());
        System.out.println("   Compras: " + compraService.listarTodas().size());
        System.out.println("---------------------------------------------");
    }


    private Administrador criarAdmin(String nome, String email, String senha) {
        Administrador a = new Administrador();
        a.setNome(nome);
        a.setEmail(email);
        a.setSenha(securityService.criptografarSenha(senha));
        return administradorRepository.save(a);
    }

    private void limparBancoDeDados() {
        clienteRepository.deleteAll();
        vendedorRepository.deleteAll();
        produtoRepository.deleteAll();
        compraRepository.deleteAll();
        administradorRepository.deleteAll();
        System.out.println("✓ Banco de dados limpo.");
    }

    private Cliente criarCliente(String nome, String cpf, String email, String senha) {
        Cliente c = new Cliente();
        c.setNome(nome);
        c.setCpf(cpf);
        c.setEmail(email);
        c.setSenha(senha);

        Endereco end = new Endereco();
        end.setEnderecoId(UUID.randomUUID().toString());
        end.setCidade("SJCampos"); end.setEstado("SP"); end.setPrincipal(true);

        return clienteService.criarCliente(c, Arrays.asList(end));
    }

    private Vendedor criarVendedor(String nome, String cpf, String nomeLoja, String cnpj, String email, String senha) {
        Vendedor v = new Vendedor();
        v.setNome(nome); v.setCpf(cpf);
        v.setNomeLoja(nomeLoja); v.setCnpj(cnpj);
        v.setEmail(email);
        v.setSenha(senha);

        Endereco end = new Endereco();
        end.setEnderecoId(UUID.randomUUID().toString());
        end.setCidade("São Paulo"); end.setEstado("SP"); end.setPrincipal(true);
        v.setEnderecos(Arrays.asList(end));

        return vendedorService.criarVendedor(v);
    }

    private Produto criarProduto(String nome, String descricao, double preco, String vendedorId) {
        Produto p = new Produto();
        p.setNome(nome);
        p.setDescricao(descricao);
        p.setPreco(preco);

        try {
            ProdutoRedis redis = produtoService.criarProduto(p, vendedorId);

            p.setId(redis.getId());

            return p;
        } catch (IllegalArgumentException e) {
            System.err.println("Erro ao criar produto: " + e.getMessage());
            return null;
        }
    }
}