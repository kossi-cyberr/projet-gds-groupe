package com.example.versuion.jwt;

import com.example.versuion.models.auth.ExtendedUser;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private static final String SECRET = "CleDeTestTresLonguePourLesTestsUnitairesAuMoins32Caracteres!";
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET, 60_000L); // 60 s
    }

    private ExtendedUser utilisateur(Long idEntreprise) {
        return new ExtendedUser("achille.mballa@novatra-distribution.cm", "secret",
                idEntreprise, List.of(new SimpleGrantedAuthority("ADMIN")));
    }

    @Test
    void genereUnTokenAvecLeBonSujet() {
        String token = jwtUtil.generateToken(utilisateur(900L));
        assertEquals("achille.mballa@novatra-distribution.cm", jwtUtil.extractUsername(token));
    }

    @Test
    void tokenContientLIdEntreprise() {
        String token = jwtUtil.generateToken(utilisateur(900L));
        assertEquals("900", jwtUtil.extractIdEntreprise(token));
    }

    @Test
    void tokenSansEntrepriseNAPasDeClaim() {
        String token = jwtUtil.generateToken(utilisateur(null));
        assertNull(jwtUtil.extractIdEntreprise(token));
    }

    @Test
    void tokenNonExpireEstValide() {
        String token = jwtUtil.generateToken(utilisateur(900L));
        ExtendedUser u = utilisateur(900L);
        assertTrue(jwtUtil.validateToken(token, u));
    }

    @Test
    void expirationProcheDeLaDureeConfiguree() {
        long avant = System.currentTimeMillis();
        String token = jwtUtil.generateToken(utilisateur(900L));
        Date expiration = jwtUtil.extractExpiration(token);
        long duree = expiration.getTime() - avant;
        assertTrue(duree > 55_000 && duree <= 60_000, "Durée attendue ~60s, obtenue " + duree + "ms");
    }

    @Test
    void claimsLisiblesEtCoherents() {
        String token = jwtUtil.generateToken(utilisateur(900L));
        Claims claims = jwtUtil.extractClaim(token, c -> c);
        assertNotNull(claims.getIssuedAt());
        assertTrue(claims.getExpiration().after(claims.getIssuedAt()));
    }
}
