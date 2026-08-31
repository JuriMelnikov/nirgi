package ee.jvm.nirgi_java.config;

import ee.jvm.nirgi_java.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.firewall.StrictHttpFirewall;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(CorsProperties.class)
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsProperties corsProperties;

    @Bean
    public StrictHttpFirewall httpFirewall() {
        return new StrictHttpFirewall();
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.setAllowedOrigins(List.of(corsProperties.allowedOrigins().split(",")));
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        return http
                .csrf(CsrfConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; " +
                                "script-src 'self' 'unsafe-inline' https://cdn.tailwindcss.com; " +
                                "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net; " +
                                "img-src 'self' data: https:; " +
                                "font-src 'self' https:; " +
                                "connect-src 'self' https://cdn.jsdelivr.net"
                        )))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/login").permitAll()
                        .requestMatchers("/static/**").permitAll()
                        .requestMatchers("/js/**").permitAll()
                        .requestMatchers("/css/**").permitAll()
                        .requestMatchers("/images/**").permitAll()
                        .requestMatchers("/favicon.ico").permitAll()
                        .requestMatchers("/favicon").permitAll()
                        // HTML pages - accessible to all, frontend handles role-based access
                        .requestMatchers("/employees").permitAll()
                        .requestMatchers("/models").permitAll()
                        .requestMatchers("/orders").permitAll()
                        .requestMatchers("/work-results").permitAll()
                        .requestMatchers("/salary").permitAll()
                        // API endpoints - protected by roles
                        // EMPLOYEE: work-results only
                        // MASTER: work-results + orders
                        // TECHNOLOGIST: work-results + orders + models/operations
                        // MANAGER: work-results + orders + models/operations + employees + salary
                        // ACCOUNTANT: salary only
                        // ADMINISTRATOR: full access
                        .requestMatchers("/api/employees/**").hasAnyAuthority("ROLE_ADMINISTRATOR", "ROLE_MANAGER", "ROLE_EMPLOYEE")
                        .requestMatchers("/api/settings/**").hasAnyAuthority("ROLE_ADMINISTRATOR", "ROLE_MANAGER")
                        .requestMatchers("/api/model-list/**").hasAnyAuthority("ROLE_ADMINISTRATOR", "ROLE_TECHNOLOGIST", "ROLE_MANAGER", "ROLE_EMPLOYEE")
                        .requestMatchers("/api/section-lists/**").hasAnyAuthority("ROLE_ADMINISTRATOR", "ROLE_TECHNOLOGIST", "ROLE_MANAGER", "ROLE_EMPLOYEE")
                        .requestMatchers("/api/techmaps/**").hasAnyAuthority("ROLE_ADMINISTRATOR", "ROLE_TECHNOLOGIST", "ROLE_MANAGER", "ROLE_EMPLOYEE")
                        .requestMatchers("/api/orders/**").hasAnyAuthority("ROLE_ADMINISTRATOR", "ROLE_MASTER", "ROLE_TECHNOLOGIST", "ROLE_MANAGER", "ROLE_EMPLOYEE")
                        .requestMatchers("/api/dubl-orders/**").hasAnyAuthority("ROLE_ADMINISTRATOR", "ROLE_MASTER", "ROLE_TECHNOLOGIST", "ROLE_MANAGER", "ROLE_EMPLOYEE")
                        .requestMatchers("/api/work-results/**").hasAnyAuthority("ROLE_ADMINISTRATOR", "ROLE_EMPLOYEE", "ROLE_MASTER", "ROLE_TECHNOLOGIST", "ROLE_MANAGER")
                        .requestMatchers("/api/salary/**").hasAnyAuthority("ROLE_ADMINISTRATOR", "ROLE_ACCOUNTANT", "ROLE_MANAGER")
                        .anyRequest().authenticated()
                )
                .securityMatcher("/**")
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
