package ee.jvm.nirgi_java.controller;

import ee.jvm.nirgi_java.dto.AuthResponse;
import ee.jvm.nirgi_java.dto.LoginRequest;
import ee.jvm.nirgi_java.security.JwtUtil;
import ee.jvm.nirgi_java.security.UserDetailsServiceImpl;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserDetailsServiceImpl userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getLogin(), loginRequest.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails);

            Set<String> roles = userDetails.getAuthorities().stream()
                    .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                    .collect(Collectors.toSet());

            System.out.println("User logged in: " + userDetails.getUsername());
            System.out.println("User roles: " + roles);

            AuthResponse response = new AuthResponse(token, userDetails.getUsername(), roles);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Login failed for user: " + loginRequest.getLogin());
            e.printStackTrace();
            return ResponseEntity.status(401).body("{\"error\": \"Неверный логин или пароль\"}");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok("Выполнен выход из системы");
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("{\"error\": \"Не авторизован\"}");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(principal.getName());
        Set<String> roles = userDetails.getAuthorities().stream()
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                .collect(Collectors.toSet());

        AuthResponse response = new AuthResponse(null, principal.getName(), roles);
        return ResponseEntity.ok(response);
    }
}
