package ee.jvm.nirgi_java.config;

import ee.jvm.nirgi_java.classes.Employee;
import ee.jvm.nirgi_java.repository.UserRepository;
import ee.jvm.nirgi_java.repository.EmployeeRepository;
import ee.jvm.nirgi_java.security.Role;
import ee.jvm.nirgi_java.security.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Component
public class AdminInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
                System.out.println("Administrator user already exists");
            }
            
//            // Create Master user
//            if (!userRepository.existsByLogin("master")) {
//                createMasterUser();
//            } else {
//                System.out.println("Master user already exists");
//            }
//
//            // Create Technologist user
//            if (!userRepository.existsByLogin("technologist")) {
//                createTechnologistUser();
//            } else {
//                System.out.println("Technologist user already exists");
//            }
//
//            // Create Manager user
//            if (!userRepository.existsByLogin("manager")) {
//                createManagerUser();
//            } else {
//                System.out.println("Manager user already exists");
//            }
//
//            // Create Employee user
//            if (!userRepository.existsByLogin("employee")) {
//                createEmployeeUser();
//            } else {
//                System.out.println("Employee user already exists");
//            }
//
//            // Create Accountant user
//            if (!userRepository.existsByLogin("accountant")) {
//                createAccountantUser();
//            } else {
//                System.out.println("Accountant user already exists");
//            }
        } catch (Exception e) {
            System.err.println("Error creating default users: " + e.getMessage());
            e.printStackTrace();
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

        System.out.println("Default administrator created successfully");
        System.out.println("Login: Administrator");
        System.out.println("Password: 12345");
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

        System.out.println("Master user created successfully");
        System.out.println("Login: admin");
        System.out.println("Password: 12345");
    }


//    private void createMasterUser() {
//        Employee employee = new Employee();
//        employee.setName("Master");
//        employee.setSurname("User");
//        employee.setDay(1);
//        employee.setMonth(1);
//        employee.setYear(1990);
//        employee.setPhone("+37256509988");
//        employee.setCountry("");
//        employee.setCity("");
//        employee.setStreet("");
//        employee.setHouse("");
//        employee.setRoom("");
//        employee.setActive(true);
//
//        Employee savedEmployee = employeeRepository.save(employee);
//
//        User masterUser = new User();
//        masterUser.setLogin("master");
//        masterUser.setPassword(passwordEncoder.encode("123"));
//        masterUser.setCreatedAt(LocalDateTime.now());
//        masterUser.setEmployee(savedEmployee);
//
//        Set<Role> masterRoles = new HashSet<>();
//        masterRoles.add(Role.MASTER);
//        masterRoles.add(Role.EMPLOYEE);
//        masterUser.setRoles(masterRoles);
//
//        User savedUser = userRepository.save(masterUser);
//        savedEmployee.setUser(savedUser);
//        employeeRepository.save(savedEmployee);
//
//        System.out.println("Master user created successfully");
//        System.out.println("Login: master");
//        System.out.println("Password: 123");
//    }
//
//    private void createTechnologistUser() {
//        Employee employee = new Employee();
//        employee.setName("Technologist");
//        employee.setSurname("User");
//        employee.setDay(1);
//        employee.setMonth(1);
//        employee.setYear(1990);
//        employee.setPhone("+37256509989");
//        employee.setCountry("");
//        employee.setCity("");
//        employee.setStreet("");
//        employee.setHouse("");
//        employee.setRoom("");
//        employee.setActive(true);
//
//        Employee savedEmployee = employeeRepository.save(employee);
//
//        User technologistUser = new User();
//        technologistUser.setLogin("technologist");
//        technologistUser.setPassword(passwordEncoder.encode("123"));
//        technologistUser.setCreatedAt(LocalDateTime.now());
//        technologistUser.setEmployee(savedEmployee);
//
//        Set<Role> technologistRoles = new HashSet<>();
//        technologistRoles.add(Role.TECHNOLOGIST);
//        technologistRoles.add(Role.EMPLOYEE);
//        technologistRoles.add(Role.MASTER);
//        technologistUser.setRoles(technologistRoles);
//
//        User savedUser = userRepository.save(technologistUser);
//        savedEmployee.setUser(savedUser);
//        employeeRepository.save(savedEmployee);
//
//        System.out.println("Technologist user created successfully");
//        System.out.println("Login: technologist");
//        System.out.println("Password: 123");
//    }
//
//    private void createManagerUser() {
//        Employee employee = new Employee();
//        employee.setName("Manager");
//        employee.setSurname("User");
//        employee.setDay(1);
//        employee.setMonth(1);
//        employee.setYear(1990);
//        employee.setPhone("+37256509990");
//        employee.setCountry("");
//        employee.setCity("");
//        employee.setStreet("");
//        employee.setHouse("");
//        employee.setRoom("");
//        employee.setActive(true);
//
//        Employee savedEmployee = employeeRepository.save(employee);
//
//        User managerUser = new User();
//        managerUser.setLogin("manager");
//        managerUser.setPassword(passwordEncoder.encode("123"));
//        managerUser.setCreatedAt(LocalDateTime.now());
//        managerUser.setEmployee(savedEmployee);
//
//        Set<Role> managerRoles = new HashSet<>();
//        managerRoles.add(Role.MANAGER);
//        managerRoles.add(Role.TECHNOLOGIST);
//        managerRoles.add(Role.EMPLOYEE);
//        managerRoles.add(Role.MASTER);
//        managerRoles.add(Role.ACCOUNTANT);
//        managerUser.setRoles(managerRoles);
//
//        User savedUser = userRepository.save(managerUser);
//        savedEmployee.setUser(savedUser);
//        employeeRepository.save(savedEmployee);
//
//        System.out.println("Manager user created successfully");
//        System.out.println("Login: manager");
//        System.out.println("Password: 123");
//    }
//
//    private void createEmployeeUser() {
//        Employee employee = new Employee();
//        employee.setName("Employee");
//        employee.setSurname("User");
//        employee.setDay(1);
//        employee.setMonth(1);
//        employee.setYear(1990);
//        employee.setPhone("+37256509991");
//        employee.setCountry("");
//        employee.setCity("");
//        employee.setStreet("");
//        employee.setHouse("");
//        employee.setRoom("");
//        employee.setActive(true);
//
//        Employee savedEmployee = employeeRepository.save(employee);
//
//        User employeeUser = new User();
//        employeeUser.setLogin("employee");
//        employeeUser.setPassword(passwordEncoder.encode("123"));
//        employeeUser.setCreatedAt(LocalDateTime.now());
//        employeeUser.setEmployee(savedEmployee);
//
//        Set<Role> employeeRoles = new HashSet<>();
//        employeeRoles.add(Role.EMPLOYEE);
//        employeeUser.setRoles(employeeRoles);
//
//        User savedUser = userRepository.save(employeeUser);
//        savedEmployee.setUser(savedUser);
//        employeeRepository.save(savedEmployee);
//
//        System.out.println("Employee user created successfully");
//        System.out.println("Login: employee");
//        System.out.println("Password: 123");
//    }
//
//    private void createAccountantUser() {
//        Employee employee = new Employee();
//        employee.setName("Accountant");
//        employee.setSurname("User");
//        employee.setDay(1);
//        employee.setMonth(1);
//        employee.setYear(1990);
//        employee.setPhone("+37256509992");
//        employee.setCountry("");
//        employee.setCity("");
//        employee.setStreet("");
//        employee.setHouse("");
//        employee.setRoom("");
//        employee.setActive(true);
//
//        Employee savedEmployee = employeeRepository.save(employee);
//
//        User accountantUser = new User();
//        accountantUser.setLogin("accountant");
//        accountantUser.setPassword(passwordEncoder.encode("123"));
//        accountantUser.setCreatedAt(LocalDateTime.now());
//        accountantUser.setEmployee(savedEmployee);
//
//        Set<Role> accountantRoles = new HashSet<>();
//        accountantRoles.add(Role.ACCOUNTANT);
//        accountantUser.setRoles(accountantRoles);
//
//        User savedUser = userRepository.save(accountantUser);
//        savedEmployee.setUser(savedUser);
//        employeeRepository.save(savedEmployee);
//
//        System.out.println("Accountant user created successfully");
//        System.out.println("Login: accountant");
//        System.out.println("Password: 123");
//    }
}
