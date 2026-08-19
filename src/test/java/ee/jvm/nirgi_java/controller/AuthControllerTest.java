package ee.jvm.nirgi_java.controller;

import ee.jvm.nirgi_java.dto.AuthResponse;
import ee.jvm.nirgi_java.dto.LoginRequest;
import ee.jvm.nirgi_java.security.JwtUtil;
import ee.jvm.nirgi_java.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private static final UserDetails USER = User.builder()
            .username("Administrator")
            .password("pwd")
            .authorities("ROLE_ADMINISTRATOR", "ROLE_MANAGER")
            .build();

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @InjectMocks
    private AuthController authController;

    @Test
    void loginReturnsTokenAndRolesWithoutRolePrefix() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(USER, null, USER.getAuthorities());
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtUtil.generateToken(USER)).thenReturn("jwt-token");

        ResponseEntity<?> response = authController.login(new LoginRequest("Administrator", "12345"));

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        AuthResponse body = (AuthResponse) response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getToken()).isEqualTo("jwt-token");
        assertThat(body.getLogin()).isEqualTo("Administrator");
        assertThat(body.getRoles()).containsExactlyInAnyOrder("ADMINISTRATOR", "MANAGER");
    }

    @Test
    void loginWithBadCredentialsReturnsUnauthorized() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad credentials"));

        ResponseEntity<?> response = authController.login(new LoginRequest("Administrator", "wrong"));

        assertThat(response.getStatusCode().value()).isEqualTo(401);
        assertThat(response.getBody()).asString().contains("Неверный логин или пароль");
    }

    @Test
    void logoutIsAlwaysSuccessful() {
        assertThat(authController.logout().getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void currentUserRequiresPrincipal() {
        ResponseEntity<?> response = authController.getCurrentUser(null);

        assertThat(response.getStatusCode().value()).isEqualTo(401);
        assertThat(response.getBody()).asString().contains("Не авторизован");
    }

    @Test
    void currentUserReturnsRolesWithoutToken() {
        when(userDetailsService.loadUserByUsername("Administrator")).thenReturn(USER);

        ResponseEntity<?> response = authController.getCurrentUser(() -> "Administrator");

        AuthResponse body = (AuthResponse) response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getToken()).isNull();
        assertThat(body.getLogin()).isEqualTo("Administrator");
        assertThat(body.getRoles()).containsExactlyInAnyOrder("ADMINISTRATOR", "MANAGER");
    }
}
