package ee.jvm.nirgi_java.config;

import ee.jvm.nirgi_java.classes.Employee;
import ee.jvm.nirgi_java.repository.UserRepository;
import ee.jvm.nirgi_java.repository.EmployeeRepository;
import ee.jvm.nirgi_java.security.Role;
import ee.jvm.nirgi_java.security.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Component
@Slf4j
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminInitializer(UserRepository userRepository,
                            EmployeeRepository employeeRepository,
                            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Always create default users if they don't exist
        createDefaultUsers();
    }

    private void createDefaultUsers() {
        try {
            // Create Administrator with all roles
            if (!userRepository.existsByLogin("Administrator")) {
                createAdministrator();
                createAdmin();
            } else {
                log.debug("Administrator user already exists");
            }
        } catch (Exception e) {
            log.error("Error creating default users: {}", e.getMessage(), e);
        }
    }

    private void createAdministrator() {
        Employee employee = new Employee();
        employee.setName("Юрий");
        employee.setSurname("Мельников");
        employee.setDay(19);
        employee.setMonth(7);
        employee.setYear(1960);
        employee.setPhone("+37256509987");
        employee.setCity("Йыхви");
        employee.setStreet("Нарва мнт.");
        employee.setHouse("80");
        employee.setRoom("31");
        employee.setActive(true);

        Employee savedEmployee = employeeRepository.save(employee);

        User adminUser = new User();
        adminUser.setLogin("Administrator");
        adminUser.setPassword(passwordEncoder.encode("12345"));
        adminUser.setCreatedAt(LocalDateTime.now());
        adminUser.setEmployee(savedEmployee);

        Set<Role> administratorRoles = new HashSet<>();
        administratorRoles.add(Role.ADMINISTRATOR);
        administratorRoles.add(Role.MANAGER);
        administratorRoles.add(Role.TECHNOLOGIST);
        administratorRoles.add(Role.MASTER);
        administratorRoles.add(Role.ACCOUNTANT);
        administratorRoles.add(Role.EMPLOYEE);
        adminUser.setRoles(administratorRoles);

        User savedUser = userRepository.save(adminUser);
        savedEmployee.setUser(savedUser);
        employeeRepository.save(savedEmployee);

        log.info("Default administrator created successfully");
        log.info("Login: Administrator");
        log.info("Password: 12345");
    }
    private void createAdmin() {
        Employee employee = new Employee();
        employee.setName("Margarita");
        employee.setSurname("Korotajeva");
        employee.setDay(1);
        employee.setMonth(1);
        employee.setYear(1990);
        employee.setPhone("+37253407732");
        employee.setCity("1");
        employee.setStreet("1");
        employee.setHouse("1");
        employee.setRoom("1");
        employee.setActive(true);

        Employee savedEmployee = employeeRepository.save(employee);

        User adminUser = new User();
        adminUser.setLogin("admin");
        adminUser.setPassword(passwordEncoder.encode("12345"));
        adminUser.setCreatedAt(LocalDateTime.now());
        adminUser.setEmployee(savedEmployee);

        Set<Role> adminRoles = new HashSet<>();
        adminRoles.add(Role.ADMINISTRATOR);
        adminRoles.add(Role.MANAGER);
        adminRoles.add(Role.TECHNOLOGIST);
        adminRoles.add(Role.MASTER);
        adminRoles.add(Role.ACCOUNTANT);
        adminRoles.add(Role.EMPLOYEE);
        adminUser.setRoles(adminRoles);

        User savedUser = userRepository.save(adminUser);
        savedEmployee.setUser(savedUser);
        employeeRepository.save(savedEmployee);

        log.info("Master user created successfully");
        log.info("Login: admin");
        log.info("Password: 12345");
    }
}
