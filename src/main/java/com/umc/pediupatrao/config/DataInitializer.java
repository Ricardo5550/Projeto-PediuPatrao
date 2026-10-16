package com.umc.pediupatrao.config;

import com.umc.pediupatrao.entity.Usuario;
import com.umc.pediupatrao.enums.Role;
import com.umc.pediupatrao.repository.UsuarioRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(
            UsuarioRepository repo,
            PasswordEncoder passwordEncoder) {

        return args -> {

            var adminExistente = repo.findByUsername("admin");

            if (adminExistente.isPresent()) {

                Usuario admin = adminExistente.get();

                if (admin.getRole() == null) {
                    admin.setRole(Role.ADMIN);
                    repo.save(admin);

                    System.out.println(
                            "Perfil do administrador atualizado!"
                    );
                }

            } else {

                String senhaInicial =
                        System.getenv("ADMIN_INITIAL_PASSWORD");

                if (senhaInicial == null ||
                        senhaInicial.isBlank()) {
                    throw new IllegalStateException("Configure ADMIN_INITIAL_PASSWORD " + "para criar o administrador inicial.");
                }

                Usuario admin = new Usuario();

                admin.setUsername("admin");
                admin.setPassword(
                        passwordEncoder.encode(senhaInicial)
                );
                admin.setRole(Role.ADMIN);

                repo.save(admin);

                System.out.println("Usuário administrador criado!");
            }
        };
    }
}