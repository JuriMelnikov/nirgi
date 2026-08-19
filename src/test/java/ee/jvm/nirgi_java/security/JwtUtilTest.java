package ee.jvm.nirgi_java.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilTest {

    private static final String SECRET = "testSecretKeyForJWTTokenGenerationThatIsLongEnoughForHS256Algorithm";

    private JwtUtil jwtUtil;

    private static UserDetails userDetails(String username, String... authorities) {
        return User.builder().username(username).password("pwd").authorities(authorities).build();
    }

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", 60_000L);
    }

    @Test
    void generatedTokenCarriesSubjectRolesAndExpiration() {
        UserDetails user = userDetails("Administrator", "ROLE_ADMINISTRATOR", "ROLE_MANAGER");

        String token = jwtUtil.generateToken(user);

        assertThat(jwtUtil.extractUsername(token)).isEqualTo("Administrator");
        assertThat(jwtUtil.isTokenExpired(token)).isFalse();
        assertThat(jwtUtil.extractExpiration(token)).isAfter(new Date());

        var claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(SECRET.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
        assertThat(claims.get("roles", java.util.List.class))
                .containsExactlyInAnyOrder("ROLE_ADMINISTRATOR", "ROLE_MANAGER");
    }

    @Test
    void validateTokenAcceptsMatchingUser() {
        String token = jwtUtil.generateToken(userDetails("Administrator", "ROLE_ADMINISTRATOR"));

        assertThat(jwtUtil.validateToken(token, userDetails("Administrator", "ROLE_ADMINISTRATOR"))).isTrue();
    }

    @Test
    void validateTokenRejectsDifferentUser() {
        String token = jwtUtil.generateToken(userDetails("Administrator", "ROLE_ADMINISTRATOR"));

        assertThat(jwtUtil.validateToken(token, userDetails("someoneElse", "ROLE_EMPLOYEE"))).isFalse();
    }

    @Test
    void expiredTokenIsReportedAsExpired() {
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", -1_000L);
        String token = jwtUtil.generateToken(userDetails("Administrator", "ROLE_ADMINISTRATOR"));

        assertThatThrownBy(() -> jwtUtil.isTokenExpired(token)).isInstanceOf(ExpiredJwtException.class);
        assertThatThrownBy(() -> jwtUtil.extractUsername(token)).isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void tokenSignedWithAnotherSecretIsRejected() {
        JwtUtil otherIssuer = new JwtUtil();
        ReflectionTestUtils.setField(otherIssuer, "jwtSecret", "aCompletelyDifferentSecretThatIsAlsoLongEnoughForHS256!!");
        ReflectionTestUtils.setField(otherIssuer, "jwtExpiration", 60_000L);
        String foreignToken = otherIssuer.generateToken(userDetails("Administrator", "ROLE_ADMINISTRATOR"));

        assertThatThrownBy(() -> jwtUtil.extractUsername(foreignToken)).isInstanceOf(SignatureException.class);
    }
}
