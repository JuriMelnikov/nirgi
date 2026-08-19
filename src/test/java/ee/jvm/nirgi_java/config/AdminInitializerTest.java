package ee.jvm.nirgi_java.config;

import ee.jvm.nirgi_java.classes.Employee;
import ee.jvm.nirgi_java.repository.EmployeeRepository;
import ee.jvm.nirgi_java.repository.UserRepository;
import ee.jvm.nirgi_java.security.Role;
import ee.jvm.nirgi_java.security.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminInitializerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminInitializer adminInitializer;

    @Test
    void createsBothDefaultAdminAccountsOnFirstRun() throws Exception {
        when(userRepository.existsByLogin("Administrator")).thenReturn(false);
        when(passwordEncoder.encode("12345")).thenReturn("encoded");
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        adminInitializer.run();

        ArgumentCaptor<User> users = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(2)).save(users.capture());

        assertThat(users.getAllValues()).extracting(User::getLogin).containsExactly("Administrator", "admin");
        assertThat(users.getAllValues()).allSatisfy(user -> {
            assertThat(user.getPassword()).isEqualTo("encoded");
            assertThat(user.getCreatedAt()).isNotNull();
            assertThat(user.getEmployee()).isNotNull();
            assertThat(user.getRoles()).containsExactlyInAnyOrderElementsOf(List.of(Role.values()));
        });

        // employee saved once before and once after linking the user
        verify(employeeRepository, times(4)).save(any(Employee.class));
        assertThat(users.getAllValues())
                .allSatisfy(user -> assertThat(user.getEmployee().getUser()).isSameAs(user));
    }

    @Test
    void skipsCreationWhenAdministratorAlreadyExists() throws Exception {
        when(userRepository.existsByLogin("Administrator")).thenReturn(true);

        adminInitializer.run();

        verify(userRepository, never()).save(any());
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void repositoryFailureDoesNotBreakStartup() {
        when(userRepository.existsByLogin("Administrator")).thenThrow(new RuntimeException("db down"));

        assertThatCode(() -> adminInitializer.run()).doesNotThrowAnyException();
    }
}
