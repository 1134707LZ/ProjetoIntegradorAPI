package br.edu.atitus.authapi.configs;

import br.edu.atitus.authapi.entities.UserEntity;
import br.edu.atitus.authapi.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Cria o administrador didático na primeira execução (admin@admin.dev / teste). */
@Configuration
public class AdminSeed {
    @Bean
    CommandLineRunner seedAdmin(UserRepository repo, PasswordEncoder encoder) {
        return args -> {
            if (!repo.existsByEmail("admin@admin.dev")) {
                var u = new UserEntity();
                u.setName("Administrador");
                u.setEmail("admin@admin.dev");
                u.setPassword(encoder.encode("teste"));
                u.setType(UserEntity.ADMIN);
                repo.save(u);
            }
        };
    }
}
