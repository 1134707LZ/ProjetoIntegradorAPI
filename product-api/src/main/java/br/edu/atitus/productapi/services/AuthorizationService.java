package br.edu.atitus.productapi.services;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Autorização (o gateway já fez a autenticação e propagou X-User-*).
 * type: 0 = administrador, 1 = usuário comum.
 */
@Service
public class AuthorizationService {
    private static final int ADMIN = 0;

    public void requireAuthenticated(Long userId) {
        if (userId == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identidade ausente");
    }

    public void requireAdmin(Integer type) {
        if (type == null || type != ADMIN)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuário sem permissão para esta operação");
    }

    /** Dono do recurso OU administrador. */
    public void requireOwnerOrAdmin(Long ownerId, Long userId, Integer type) {
        boolean admin = type != null && type == ADMIN;
        if (!admin && (ownerId == null || !ownerId.equals(userId)))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não é o dono deste recurso");
    }
}
