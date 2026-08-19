package ee.jvm.nirgi_java.security;

import ee.jvm.nirgi_java.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.EnumSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void rolesAreMappedToPrefixedAuthorities() {
        User user = new User();
        user.setLogin("Administrator");
        user.setPassword("hashed");
        user.setRoles(EnumSet.of(Role.ADMINISTRATOR, Role.ACCOUNTANT));
        when(userRepository.findByLogin("Administrator")).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadUserByUsername("Administrator");

        assertThat(details.getUsername()).isEqualTo("Administrator");
        assertThat(details.getPassword()).isEqualTo("hashed");
        assertThat(details.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_ADMINISTRATOR", "ROLE_ACCOUNTANT");
    }

    @Test
    void unknownLoginIsRejected() {
        when(userRepository.findByLogin("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("ghost"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("ghost");
    }
}
