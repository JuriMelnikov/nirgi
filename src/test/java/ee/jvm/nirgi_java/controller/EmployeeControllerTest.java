package ee.jvm.nirgi_java.controller;

import ee.jvm.nirgi_java.classes.Employee;
import ee.jvm.nirgi_java.repository.EmployeeRepository;
import ee.jvm.nirgi_java.repository.UserRepository;
import ee.jvm.nirgi_java.repository.WorkResultRepository;
import ee.jvm.nirgi_java.security.Role;
import ee.jvm.nirgi_java.security.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EmployeeControllerTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private WorkResultRepository workResultRepository;

    @InjectMocks
    private EmployeeController employeeController;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private static void authenticateAs(String login, String... authorities) {
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(login)
                .password("pwd")
                .authorities(authorities)
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
    }

    private static Employee employee(Long id, String name, String login, Role... roles) {
        Employee employee = new Employee(id, name, name + "ov", 1, 1, 1990,
                "+372" + id, "Jõhvi", "Narva mnt.", "80", "31");
        if (login != null) {
            User user = new User();
            user.setId(id);
            user.setLogin(login);
            user.setEmployee(employee);
            user.setRoles(roles.length == 0 ? Set.of() : EnumSet.copyOf(List.of(roles)));
            employee.setUser(user);
        }
        return employee;
    }

    @Test
    void employeeOnlyUserSeesOnlyOwnRecord() {
        authenticateAs("worker", "EMPLOYEE");
        Employee worker = employee(1L, "Worker", "worker", Role.EMPLOYEE);
        when(userRepository.findByLogin("worker")).thenReturn(Optional.of(worker.getUser()));

        List<Employee> result = employeeController.getAllEmployees(null);

        assertThat(result).containsExactly(worker);
        assertThat(result.get(0).getLogin()).isEqualTo("worker");
        assertThat(result.get(0).getRoles()).containsExactly(Role.EMPLOYEE);
        verify(employeeRepository, never()).findAll();
    }

    @Test
    void employeeOnlyUserWithoutAccountGetsEmptyList() {
        authenticateAs("ghost", "EMPLOYEE");
        when(userRepository.findByLogin("ghost")).thenReturn(Optional.empty());

        assertThat(employeeController.getAllEmployees(null)).isEmpty();
    }

    @Test
    void managerListHidesBuiltInAdminAccounts() {
        authenticateAs("manager", "MANAGER", "EMPLOYEE");
        Employee administrator = employee(1L, "Root", "Administrator", Role.ADMINISTRATOR);
        Employee admin = employee(2L, "Admin", "admin", Role.ADMINISTRATOR);
        Employee worker = employee(3L, "Worker", "worker", Role.EMPLOYEE);
        Employee noAccount = employee(4L, "Casual", null);
        when(employeeRepository.findAll()).thenReturn(List.of(administrator, admin, worker, noAccount));

        assertThat(employeeController.getAllEmployees(null)).containsExactly(worker, noAccount);
        assertThat(worker.getRoles()).containsExactly(Role.EMPLOYEE);
    }

    @Test
    void administratorSeesEveryoneAndAdminSeesEveryoneButAdministrator() {
        Employee administrator = employee(1L, "Root", "Administrator", Role.ADMINISTRATOR);
        Employee admin = employee(2L, "Admin", "admin", Role.ADMINISTRATOR);
        Employee worker = employee(3L, "Worker", "worker", Role.EMPLOYEE);
        when(employeeRepository.findByActiveTrue()).thenReturn(List.of(administrator, admin, worker));

        authenticateAs("Administrator", "ADMINISTRATOR");
        assertThat(employeeController.getAllEmployees(true)).containsExactly(administrator, admin, worker);

        authenticateAs("admin", "ADMINISTRATOR");
        assertThat(employeeController.getAllEmployees(true)).containsExactly(admin, worker);
    }

    @Test
    void anonymousListingFallsBackToAllActiveEmployees() {
        Employee worker = employee(3L, "Worker", "worker", Role.EMPLOYEE);
        when(employeeRepository.findByActiveTrue()).thenReturn(List.of(worker));

        assertThat(employeeController.getAllEmployees(true)).containsExactly(worker);
    }

    @Test
    void getByIdPopulatesLoginAndRoles() {
        authenticateAs("Administrator", "ADMINISTRATOR");
        Employee worker = employee(3L, "Worker", "worker", Role.EMPLOYEE, Role.MASTER);
        when(employeeRepository.findById(3L)).thenReturn(Optional.of(worker));

        ResponseEntity<Employee> response = employeeController.getWEmployeeById(3L);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getLogin()).isEqualTo("worker");
        assertThat(response.getBody().getRoles()).containsExactlyInAnyOrder(Role.EMPLOYEE, Role.MASTER);
    }

    @Test
    void getByIdForbidsReadingBuiltInAccountsAndReportsMissingEmployee() {
        authenticateAs("manager", "MANAGER");
        Employee administrator = employee(1L, "Root", "Administrator", Role.ADMINISTRATOR);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(administrator));
        when(employeeRepository.findById(404L)).thenReturn(Optional.empty());

        assertThat(employeeController.getWEmployeeById(1L).getStatusCode().value()).isEqualTo(403);
        assertThat(employeeController.getWEmployeeById(404L).getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void getByPhoneAppliesTheSameVisibilityRules() {
        Employee administrator = employee(1L, "Root", "Administrator", Role.ADMINISTRATOR);
        when(employeeRepository.findByPhone("+3721")).thenReturn(Optional.of(administrator));
        when(employeeRepository.findByPhone("+372404")).thenReturn(Optional.empty());

        authenticateAs("manager", "MANAGER");
        assertThat(employeeController.getEmployeeByPhone("+3721").getStatusCode().value()).isEqualTo(403);

        authenticateAs("Administrator", "ADMINISTRATOR");
        assertThat(employeeController.getEmployeeByPhone("+3721").getStatusCode().value()).isEqualTo(200);
        assertThat(employeeController.getEmployeeByPhone("+372404").getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void searchEndpointsFilterBuiltInAccounts() {
        authenticateAs("worker", "EMPLOYEE", "MASTER");
        Employee administrator = employee(1L, "Root", "Administrator", Role.ADMINISTRATOR);
        Employee admin = employee(2L, "Admin", "admin", Role.ADMINISTRATOR);
        Employee worker = employee(3L, "Worker", "worker", Role.EMPLOYEE);
        List<Employee> all = List.of(administrator, admin, worker);
        when(employeeRepository.findBySurname("Workerov")).thenReturn(all);
        when(employeeRepository.findByCity("Jõhvi")).thenReturn(all);
        when(employeeRepository.findByNameContaining("Work")).thenReturn(all);
        when(employeeRepository.findByFilters("Work", null, null)).thenReturn(all);

        assertThat(employeeController.getEmployeesBySurname("Workerov")).containsExactly(worker);
        assertThat(employeeController.getEmployeesByLocation("Jõhvi")).containsExactly(worker);
        assertThat(employeeController.getEmployeesByName("Work")).containsExactly(worker);
        assertThat(employeeController.getEmployeesByFilters("Work", null, null)).containsExactly(worker);
    }

    @Test
    void createEmployeeGeneratesLoginHashesPasswordAndAppliesRoleInheritance() {
        Employee request = employee(null, "Worker", null);
        request.setPassword("secret");
        request.setRoles(Set.of(Role.TECHNOLOGIST));
        when(employeeRepository.findByPhone(request.getPhone())).thenReturn(Optional.empty());
        when(userRepository.existsByLogin("worker.workerov")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("hashed");
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<?> response = employeeController.createEmployee(request);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        ArgumentCaptor<User> user = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(user.capture());
        assertThat(user.getValue().getLogin()).isEqualTo("worker.workerov");
        assertThat(user.getValue().getPassword()).isEqualTo("hashed");
        assertThat(user.getValue().getRoles())
                .containsExactlyInAnyOrder(Role.TECHNOLOGIST, Role.MASTER, Role.EMPLOYEE);
        assertThat(((Employee) response.getBody()).getUser()).isSameAs(user.getValue());
    }

    @Test
    void createEmployeeUsesProvidedLoginInLowerCaseAndDefaultRole() {
        Employee request = employee(null, "Worker", null);
        request.setPassword("secret");
        request.setLogin("  MyLogin  ");
        when(employeeRepository.findByPhone(request.getPhone())).thenReturn(Optional.empty());
        when(userRepository.existsByLogin("mylogin")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("hashed");
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        employeeController.createEmployee(request);

        ArgumentCaptor<User> user = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(user.capture());
        assertThat(user.getValue().getLogin()).isEqualTo("mylogin");
        assertThat(user.getValue().getRoles()).containsExactly(Role.EMPLOYEE);
    }

    @Test
    void createEmployeeRejectsDuplicatePhoneMissingPasswordAndDuplicateLogin() {
        Employee duplicatePhone = employee(null, "Worker", null);
        duplicatePhone.setPassword("secret");
        when(employeeRepository.findByPhone(duplicatePhone.getPhone()))
                .thenReturn(Optional.of(employee(9L, "Other", null)));
        assertThat(employeeController.createEmployee(duplicatePhone).getStatusCode().value()).isEqualTo(400);

        Employee noPassword = employee(null, "Worker", null);
        when(employeeRepository.findByPhone(noPassword.getPhone())).thenReturn(Optional.empty());
        assertThat(employeeController.createEmployee(noPassword).getBody())
                .isEqualTo("Пароль обязателен при создании работника");

        Employee duplicateLogin = employee(null, "Worker", null);
        duplicateLogin.setPassword("secret");
        when(userRepository.existsByLogin("worker.workerov")).thenReturn(true);
        assertThat(employeeController.createEmployee(duplicateLogin).getBody())
                .isEqualTo("Пользователь с таким логином уже существует");

        verify(employeeRepository, never()).save(any());
    }

    @Test
    void createEmployeeTranslatesDuplicateEntryFailure() {
        Employee request = employee(null, "Worker", null);
        request.setPassword("secret");
        when(employeeRepository.findByPhone(request.getPhone())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret")).thenReturn("hashed");
        when(employeeRepository.save(any(Employee.class)))
                .thenThrow(new RuntimeException("Duplicate entry '+3721' for key 'phone'"));

        ResponseEntity<?> response = employeeController.createEmployee(request);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isEqualTo("Работник с таким номером телефона уже существует");
    }

    @Test
    void updateEmployeeRefreshesFieldsRolesPasswordAndSnapshots() {
        authenticateAs("Administrator", "ADMINISTRATOR");
        Employee stored = employee(3L, "Worker", "worker", Role.EMPLOYEE);
        Employee details = employee(3L, "Renamed", null);
        details.setPassword("newSecret");
        details.setRoles(Set.of(Role.MANAGER));
        details.setActive(false);
        when(employeeRepository.findById(3L)).thenReturn(Optional.of(stored));
        when(employeeRepository.save(stored)).thenReturn(stored);
        when(passwordEncoder.encode("newSecret")).thenReturn("newHash");

        ResponseEntity<?> response = employeeController.updateEmployee(3L, details);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(stored.getName()).isEqualTo("Renamed");
        assertThat(stored.getSurname()).isEqualTo("Renamedov");
        assertThat(stored.getActive()).isFalse();
        assertThat(stored.getUser().getLogin()).isEqualTo("worker");
        assertThat(stored.getUser().getPassword()).isEqualTo("newHash");
        verify(workResultRepository).updateEmployeeNameSnapshots(3L, "Renamed", "Renamedov");
        verify(userRepository).deleteUserRoles(3L);
        verify(userRepository).insertUserRole(3L, Role.MANAGER.name());
        verify(userRepository).insertUserRole(3L, Role.EMPLOYEE.name());
    }

    @Test
    void updateEmployeeKeepsSnapshotsWhenNameIsUnchanged() {
        authenticateAs("Administrator", "ADMINISTRATOR");
        Employee stored = employee(3L, "Worker", "worker", Role.EMPLOYEE);
        Employee details = employee(3L, "Worker", null);
        when(employeeRepository.findById(3L)).thenReturn(Optional.of(stored));
        when(employeeRepository.save(stored)).thenReturn(stored);

        employeeController.updateEmployee(3L, details);

        verify(workResultRepository, never()).updateEmployeeNameSnapshots(any(), any(), any());
        verify(userRepository, never()).deleteUserRoles(any());
    }

    @Test
    void updateEmployeeProtectsBuiltInAccounts() {
        Employee administrator = employee(1L, "Root", "Administrator", Role.ADMINISTRATOR);
        Employee admin = employee(2L, "Admin", "admin", Role.ADMINISTRATOR);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(administrator));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(admin));
        when(employeeRepository.findById(404L)).thenReturn(Optional.empty());

        authenticateAs("admin", "ADMINISTRATOR");
        assertThat(employeeController.updateEmployee(1L, employee(1L, "Root", null)).getStatusCode().value())
                .isEqualTo(403);

        authenticateAs("manager", "MANAGER");
        assertThat(employeeController.updateEmployee(2L, employee(2L, "Admin", null)).getStatusCode().value())
                .isEqualTo(403);
        assertThat(employeeController.updateEmployee(404L, employee(404L, "Ghost", null)).getStatusCode().value())
                .isEqualTo(404);
    }

    @Test
    void deleteEmployeeDeactivatesInsteadOfRemoving() {
        authenticateAs("Administrator", "ADMINISTRATOR");
        Employee worker = employee(3L, "Worker", "worker", Role.EMPLOYEE);
        when(employeeRepository.findById(3L)).thenReturn(Optional.of(worker));

        assertThat(employeeController.deleteEmployee(3L).getStatusCode().value()).isEqualTo(200);
        assertThat(worker.getActive()).isFalse();
        verify(employeeRepository).save(worker);
    }

    @Test
    void deleteEmployeeProtectsBuiltInAccounts() {
        Employee administrator = employee(1L, "Root", "Administrator", Role.ADMINISTRATOR);
        Employee admin = employee(2L, "Admin", "admin", Role.ADMINISTRATOR);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(administrator));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(admin));
        when(employeeRepository.findById(404L)).thenReturn(Optional.empty());

        authenticateAs("Administrator", "ADMINISTRATOR");
        assertThat(employeeController.deleteEmployee(1L).getStatusCode().value()).isEqualTo(403);
        assertThat(employeeController.deleteEmployee(2L).getStatusCode().value()).isEqualTo(200);

        authenticateAs("manager", "MANAGER");
        assertThat(employeeController.deleteEmployee(2L).getStatusCode().value()).isEqualTo(403);
        assertThat(employeeController.deleteEmployee(404L).getStatusCode().value()).isEqualTo(404);
    }
}
