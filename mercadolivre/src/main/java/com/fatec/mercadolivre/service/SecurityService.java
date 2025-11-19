package com.fatec.mercadolivre.service;

import com.fatec.mercadolivre.model.entidades.Administrador;
import com.fatec.mercadolivre.model.entidades.Cliente;
import com.fatec.mercadolivre.model.entidades.Vendedor;
import com.fatec.mercadolivre.repository.AdministradorRepository;
import com.fatec.mercadolivre.repository.ClienteRepository;
import com.fatec.mercadolivre.repository.VendedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SecurityService {

    @Autowired private ClienteRepository clienteRepository;
    @Autowired private VendedorRepository vendedorRepository;
    @Autowired private AdministradorRepository administradorRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public String criptografarSenha(String senha) {
        return passwordEncoder.encode(senha);
    }

    public Optional<Object> autenticar(String email, String senha) {
        Optional<Administrador> adminOpt = administradorRepository.findByEmail(email);
        if (adminOpt.isPresent()) {
            Administrador admin = adminOpt.get();
            if (admin.getSenha() != null && passwordEncoder.matches(senha, admin.getSenha())) {
                return Optional.of(admin);
            }
        }

        Optional<Cliente> clienteOpt = clienteRepository.findByEmail(email);
        if (clienteOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();
            if (cliente.getSenha() != null && passwordEncoder.matches(senha, cliente.getSenha())) {
                return Optional.of(cliente);
            }
        }

        Optional<Vendedor> vendedorOpt = vendedorRepository.findByEmail(email);
        if (vendedorOpt.isPresent()) {
            Vendedor vendedor = vendedorOpt.get();
            if (vendedor.getSenha() != null && passwordEncoder.matches(senha, vendedor.getSenha())) {
                return Optional.of(vendedor);
            }
        }

        return Optional.empty();
    }
}