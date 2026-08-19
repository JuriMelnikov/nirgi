package ee.jvm.nirgi_java.security;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JwtAuthenticationFilterTest {

    private static final UserDetails USER = User.builder()
            .username("Administrator")
            .password("pwd")
            .authorities("ROLE_ADMINISTRATOR")
            .build();

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    private JwtAuthenticationFilter filter() {
        return new JwtAuthenticationFilter(jwtUtil, userDetailsService);
    }

    private static MockHttpServletRequest request(String uri, String authorizationHeader) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        request.setRequestURI(uri);
        if (authorizationHeader != null) {
            request.addHeader("Authorization", authorizationHeader);
        }
        return request;
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void publicPathsAndTheirSubPathsAreNotFiltered() {
        JwtAuthenticationFilter filter = filter();

        assertThat(filter.shouldNotFilter(request("/", null))).isTrue();
        assertThat(filter.shouldNotFilter(request("/login", null))).isTrue();
        assertThat(filter.shouldNotFilter(request("/js/app.js", null))).isTrue();
        assertThat(filter.shouldNotFilter(request("/api/auth/login", null))).isTrue();
        assertThat(filter.shouldNotFilter(request("/api/orders", null))).isFalse();
        assertThat(filter.shouldNotFilter(request("/logins", null))).isFalse();
    }

    @Test
    void validTokenPopulatesSecurityContext() throws Exception {
        when(jwtUtil.extractUsername("token")).thenReturn("Administrator");
        when(userDetailsService.loadUserByUsername("Administrator")).thenReturn(USER);
        when(jwtUtil.validateToken("token", USER)).thenReturn(true);
        FilterChain chain = mock(FilterChain.class);

        filter().doFilter(request("/api/orders", "Bearer token"), new MockHttpServletResponse(), chain);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isSameAs(USER);
        assertThat(authentication.getAuthorities()).extracting(Object::toString).containsExactly("ROLE_ADMINISTRATOR");
        verify(chain).doFilter(any(), any());
    }

    @Test
    void invalidTokenLeavesContextUnauthenticated() throws Exception {
        when(jwtUtil.extractUsername("token")).thenReturn("Administrator");
        when(userDetailsService.loadUserByUsername("Administrator")).thenReturn(USER);
        when(jwtUtil.validateToken("token", USER)).thenReturn(false);
        FilterChain chain = mock(FilterChain.class);

        filter().doFilter(request("/api/orders", "Bearer token"), new MockHttpServletResponse(), chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(any(), any());
    }

    @Test
    void expiredTokenIsIgnoredAndChainContinues() throws Exception {
        when(jwtUtil.extractUsername("token")).thenThrow(new ExpiredJwtException(null, null, "expired"));
        FilterChain chain = mock(FilterChain.class);

        filter().doFilter(request("/api/orders", "Bearer token"), new MockHttpServletResponse(), chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(userDetailsService, never()).loadUserByUsername(any());
        verify(chain).doFilter(any(), any());
    }

    @Test
    void malformedTokenIsIgnoredAndChainContinues() throws Exception {
        when(jwtUtil.extractUsername("token")).thenThrow(new RuntimeException("malformed"));
        FilterChain chain = mock(FilterChain.class);

        filter().doFilter(request("/api/orders", "Bearer token"), new MockHttpServletResponse(), chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(any(), any());
    }

    @Test
    void requestsWithoutBearerHeaderSkipAuthentication() throws Exception {
        FilterChain chain = mock(FilterChain.class);

        filter().doFilter(request("/api/orders", null), new MockHttpServletResponse(), chain);
        filter().doFilter(request("/api/orders", "Basic dXNlcjpwYXNz"), new MockHttpServletResponse(), chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtUtil, never()).extractUsername(any());
        verify(chain, org.mockito.Mockito.times(2)).doFilter(any(), any());
    }
}
