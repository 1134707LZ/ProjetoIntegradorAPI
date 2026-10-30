package br.edu.atitus.authapi.services;

import br.edu.atitus.authapi.components.JwtService;
import br.edu.atitus.authapi.dtos.Dtos.*;
import br.edu.atitus.authapi.entities.UserEntity;
import br.edu.atitus.authapi.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.regex.Pattern;

@Service
public class AuthService {
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UserRepository repo, PasswordEncoder encoder, JwtService jwt) {
        this.repo = repo; this.encoder = encoder; this.jwt = jwt;
    }

    public UserResponse signup(SignupRequest r) {
        if (r == null) throw bad("Requisição inválida");
        if (r.name() == null || r.name().isBlank()) throw bad("Nome inválido");
        if (r.email() == null || !EMAIL.matcher(r.email().trim()).matches()) throw bad("E-mail inválido");
        if (r.password() == null || r.password().length() < 6) throw bad("Senha deve ter ao menos 6 caracteres");
        String email = r.email().trim().toLowerCase();
        if (repo.existsByEmail(email)) throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado.");

        var u = new UserEntity();
        u.setName(r.name().trim());
        u.setEmail(email);
        u.setPhone(r.phone() != null ? r.phone().trim() : null);
        u.setPassword(encoder.encode(r.password()));
        u.setType(UserEntity.COMMON);   // cadastro público NUNCA cria administrador
        return toResponse(repo.save(u));
    }

    public SigninResponse signin(SigninRequest r) {
        if (r == null || r.email() == null || r.password() == null) throw bad("Credenciais inválidas");
        var u = repo.findByEmail(r.email().trim().toLowerCase())
                .filter(x -> encoder.matches(r.password(), x.getPassword()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos."));
        return new SigninResponse(jwt.generate(u), toResponse(u));
    }

    private static UserResponse toResponse(UserEntity u) {
        return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getPhone(), u.getType());
    }
    private static ResponseStatusException bad(String m) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, m); }
}
