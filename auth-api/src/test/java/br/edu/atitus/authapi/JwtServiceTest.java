package br.edu.atitus.authapi;

import br.edu.atitus.authapi.components.JwtService;
import br.edu.atitus.authapi.entities.UserEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {
    @Test
    void geraEValidaClaims() {
        var svc = new JwtService("chaveSuperSecretaParaJWTdeExemplo!@#123", 5);
        var u = new UserEntity();
        u.setId(7L); u.setEmail("a@b.com"); u.setName("A"); u.setType(0);
        var c = svc.parse(svc.generate(u));
        assertEquals(7L, c.get("id", Long.class));
        assertEquals(0, c.get("type", Integer.class));
        assertEquals("a@b.com", c.get("email", String.class));
    }

    @Test
    void rejeitaChaveDiferente() {
        var a = new JwtService("chaveSuperSecretaParaJWTdeExemplo!@#123", 5);
        var b = new JwtService("outraChaveCompletamenteDiferente-1234567890", 5);
        var u = new UserEntity(); u.setId(1L); u.setEmail("x@y.z"); u.setName("X");
        assertThrows(Exception.class, () -> b.parse(a.generate(u)));
    }
}
