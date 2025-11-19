package com.fatec.mercadolivre.service;

import com.fatec.mercadolivre.model.entidades.Endereco;
import com.fatec.mercadolivre.model.entidades.Vendedor;
import com.fatec.mercadolivre.repository.VendedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class VendedorService {

    @Autowired
    private VendedorRepository vendedorRepository;
    @Autowired
    private SecurityService securityService;

    public Vendedor criarVendedor(Vendedor vendedor) {
        if (vendedorRepository.findByCnpj(vendedor.getCnpj()).isPresent()) {
            throw new IllegalArgumentException("CNPJ já cadastrado.");
        }

        if (vendedor.getSenha() != null && !vendedor.getSenha().isEmpty()) {
            String senhaHash = securityService.criptografarSenha(vendedor.getSenha());
            vendedor.setSenha(senhaHash);
        }

        List<Endereco> enderecos = vendedor.getEnderecos();
        if (enderecos != null && !enderecos.isEmpty()) {
            boolean principalDefinido = enderecos.stream().anyMatch(Endereco::isPrincipal);

            if (!principalDefinido) {
                enderecos.get(0).setPrincipal(true);
            }
            enderecos.forEach(e -> e.setEnderecoId(UUID.randomUUID().toString()));
        }

        return vendedorRepository.save(vendedor);
    }

    public Vendedor atualizarVendedor(String id, Vendedor novosDados) {
        Optional<Vendedor> vendedorOpt = vendedorRepository.findById(id);

        if (vendedorOpt.isPresent()) {
            Vendedor vendedorAtual = vendedorOpt.get();

            if (novosDados.getNome() != null) vendedorAtual.setNome(novosDados.getNome());
            if (novosDados.getEmail() != null) vendedorAtual.setEmail(novosDados.getEmail());

            return vendedorRepository.save(vendedorAtual);
        }
        return null;
    }

    public boolean deletarVendedor(String id) {
        if (vendedorRepository.existsById(id)) {
            vendedorRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<Vendedor> listarTodos() {
        return vendedorRepository.findAll();
    }

    public Optional<Vendedor> buscarPorId(String id) {
        return vendedorRepository.findById(id);
    }
}