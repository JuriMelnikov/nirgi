package ee.jvm.nirgi_java.controller;

import ee.jvm.nirgi_java.classes.Employee;
import ee.jvm.nirgi_java.repository.UserRepository;
import ee.jvm.nirgi_java.repository.EmployeeRepository;
import ee.jvm.nirgi_java.repository.WorkResultRepository;
import ee.jvm.nirgi_java.security.Role;
import ee.jvm.nirgi_java.security.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = "*")
public class EmployeeController {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private WorkResultRepository workResultRepository;

    @GetMapping
    public List<Employee> getAllEmployees(@RequestParam(required = false) Boolean activeOnly) {
        // Get current user
        org.springframework.security.core.Authentication authentication = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        
        String currentUsername = null;
        boolean isAdministrator = false;
        
        if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
            org.springframework.security.core.userdetails.UserDetails userDetails = 
                (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal();
            
            currentUsername = userDetails.getUsername();
            isAdministrator = "Administrator".equals(currentUsername);
            
            logger.info("Current user: {}, isAdministrator: {}", currentUsername, isAdministrator);
            
            // Check if user has EMPLOYEE role ONLY (no other roles)
            boolean hasEmployeeRole = userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_EMPLOYEE"));
            boolean hasOtherRoles = userDetails.getAuthorities().stream()
                .anyMatch(auth -> 
                    auth.getAuthority().equals("ROLE_MANAGER") ||
                    auth.getAuthority().equals("ROLE_MASTER") ||
                    auth.getAuthority().equals("ROLE_TECHNOLOGIST") ||
                    auth.getAuthority().equals("ROLE_ADMINISTRATOR") ||
                    auth.getAuthority().equals("ROLE_ACCOUNTANT")
                );
            boolean isEmployeeOnly = hasEmployeeRole && !hasOtherRoles;
            
            if (isEmployeeOnly) {
                // Return only current employee
                logger.info("EMPLOYEE-only user detected: {}", currentUsername);
                return userRepository.findByLogin(currentUsername)
                    .map(user -> {
                        logger.info("User found: {}, employee: {}", user.getLogin(), 
                            user.getEmployee() != null ? user.getEmployee().getId() : "null");
                        return user.getEmployee();
                    })
                    .map(employee -> {
                        if (employee.getUser() != null) {
                            if (employee.getUser().getRoles() != null) {
                                employee.setRoles(employee.getUser().getRoles());
                            }
                            employee.setLogin(employee.getUser().getLogin());
                        }
                        logger.info("Returning employee for EMPLOYEE user: {} {} (ID: {})", 
                            employee.getName(), employee.getSurname(), employee.getId());
                        return java.util.Collections.singletonList(employee);
                    })
                    .orElse(java.util.Collections.emptyList());
            }
        }
        
        // For MANAGER and other roles, return all employees
        List<Employee> employees;
        if (activeOnly != null && activeOnly) {
            employees = employeeRepository.findByActiveTrue();
        } else {
            employees = employeeRepository.findAll();
        }
        
        logger.info("Total employees before filtering: {}", employees.size());
        
        // Exclude Administrator and admin users from the list, unless current user is Administrator or admin
        final boolean finalIsAdministrator = isAdministrator;
        final boolean finalIsAdmin = "admin".equals(currentUsername);
        employees = employees.stream()
            .filter(employee -> {
                if (employee.getUser() == null) {
                    return true;
                }
                String employeeLogin = employee.getUser().getLogin();
                logger.info("Filtering employee with login: {}", employeeLogin);
                // If current user is Administrator, include everyone
                if (finalIsAdministrator) {
                    logger.info("Including all employees (current user is Administrator)");
                    return true;
                }
                // If current user is admin, include themselves and all workers except Administrator
                if (finalIsAdmin) {
                    boolean shouldInclude = !"Administrator".equals(employeeLogin);
                    logger.info("Admin user filtering - Employee {} should be included: {}", employeeLogin, shouldInclude);
                    return shouldInclude;
                }
                // Otherwise, exclude both Administrator and admin
                boolean shouldInclude = !"Administrator".equals(employeeLogin) && !"admin".equals(employeeLogin);
                logger.info("Employee {} should be included: {}", employeeLogin, shouldInclude);
                return shouldInclude;
            })
            .toList();
        
        // Populate roles from user for each employee
        employees.forEach(employee -> {
            if (employee.getUser() != null && employee.getUser().getRoles() != null) {
                employee.setRoles(employee.getUser().getRoles());
            }
        });
        
        logger.info("Returning {} employees for user {}", employees.size(), currentUsername);
        return employees;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employee> getWEmployeeById(@PathVariable Long id) {
        return employeeRepository.findById(id)
                .map(employee -> {
                    // Check if this is Administrator or admin user and current user is not allowed
                    if (employee.getUser() != null) {
                        String employeeLogin = employee.getUser().getLogin();
                        org.springframework.security.core.Authentication authentication = 
                            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                        
                        if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
                            org.springframework.security.core.userdetails.UserDetails userDetails = 
                                (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal();
                            
                            String currentUsername = userDetails.getUsername();
                            boolean isAdministrator = "Administrator".equals(currentUsername);
                            boolean isAdmin = "admin".equals(currentUsername);
                            
                            // Administrator sees everyone
                            if (isAdministrator) {
                                // continue
                            }
                            // admin sees Administrator and themselves, but not other admins
                            else if (isAdmin && "admin".equals(employeeLogin) && !employeeLogin.equals(currentUsername)) {
                                return ResponseEntity.status(403).<Employee>build();
                            }
                            // Others don't see Administrator or admin unless it's themselves
                            else if (("Administrator".equals(employeeLogin) || "admin".equals(employeeLogin)) && !employeeLogin.equals(currentUsername)) {
                                return ResponseEntity.status(403).<Employee>build();
                            }
                        }
                    }
                    
                    // Populate roles from user if exists
                    if (employee.getUser() != null) {
                        if (employee.getUser().getRoles() != null) {
                            employee.setRoles(employee.getUser().getRoles());
                        }
                        // Populate login from user
                        employee.setLogin(employee.getUser().getLogin());
                    }
                    return ResponseEntity.ok().body(employee);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search/phone")
    public ResponseEntity<Employee> getEmployeeByPhone(@RequestParam String phone) {
        return employeeRepository.findByPhone(phone)
                .map(employee -> {
                    // Check if this is Administrator or admin user and current user is not allowed
                    if (employee.getUser() != null) {
                        String employeeLogin = employee.getUser().getLogin();
                        org.springframework.security.core.Authentication authentication = 
                            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                        
                        if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
                            org.springframework.security.core.userdetails.UserDetails userDetails = 
                                (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal();
                            
                            String currentUsername = userDetails.getUsername();
                            boolean isAdministrator = "Administrator".equals(currentUsername);
                            boolean isAdmin = "admin".equals(currentUsername);
                            
                            // Administrator sees everyone
                            if (isAdministrator) {
                                // continue
                            }
                            // admin sees Administrator and themselves, but not other admins
                            else if (isAdmin && "admin".equals(employeeLogin) && !employeeLogin.equals(currentUsername)) {
                                return ResponseEntity.status(403).<Employee>build();
                            }
                            // Others don't see Administrator or admin unless it's themselves
                            else if (("Administrator".equals(employeeLogin) || "admin".equals(employeeLogin)) && !employeeLogin.equals(currentUsername)) {
                                return ResponseEntity.status(403).<Employee>build();
                            }
                        }
                    }
                    return ResponseEntity.ok().body(employee);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search/surname")
    public List<Employee> getEmployeesBySurname(@RequestParam String surname) {
        List<Employee> employees = employeeRepository.findBySurname(surname);
        return filterAdministrator(employees);
    }

    @GetMapping("/search/location")
    public List<Employee> getEmployeesByLocation(@RequestParam String city) {
        List<Employee> employees = employeeRepository.findByCity(city);
        return filterAdministrator(employees);
    }

    @GetMapping("/search/name")
    public List<Employee> getEmployeesByName(@RequestParam String name) {
        List<Employee> employees = employeeRepository.findByNameContaining(name);
        return filterAdministrator(employees);
    }

    @GetMapping("/search")
    public List<Employee> getEmployeesByFilters(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String surname,
            @RequestParam(required = false) String city) {
        List<Employee> employees = employeeRepository.findByFilters(name, surname, city);
        return filterAdministrator(employees);
    }

    @PostMapping
    public ResponseEntity<?> createEmployee(@RequestBody Employee employee) {
        logger.info("Received employee data: {}", employee);
        try {
            // Check if phone already exists
            if (employeeRepository.findByPhone(employee.getPhone()).isPresent()) {
                return ResponseEntity.badRequest().body("Работник с таким номером телефона уже существует");
            }
            
            // Check if password is provided
            if (employee.getPassword() == null || employee.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Пароль обязателен при создании работника");
            }
            
            // Use provided login or generate from name and surname
            String login;
            if (employee.getLogin() != null && !employee.getLogin().trim().isEmpty()) {
                login = employee.getLogin().trim().toLowerCase();
            } else {
                login = employee.getName().toLowerCase() + "." + employee.getSurname().toLowerCase();
            }
            
            // Check if login already exists
            if (userRepository.existsByLogin(login)) {
                return ResponseEntity.badRequest().body("Пользователь с таким логином уже существует");
            }
            
            // Hash the password
            String hashedPassword = passwordEncoder.encode(employee.getPassword());
            
            // Create User entity
            User user = new User();
            user.setLogin(login);
            user.setPassword(hashedPassword);
            user.setCreatedAt(LocalDateTime.now());
            
            // Set roles based on access permissions
            if (employee.getRoles() != null && !employee.getRoles().isEmpty()) {
                Set<Role> assignedRoles = assignRolesBasedOnAccess(employee.getRoles());
                user.setRoles(assignedRoles);
            } else {
                // Default role for new users
                Set<Role> defaultRoles = new HashSet<>();
                defaultRoles.add(Role.EMPLOYEE);
                user.setRoles(defaultRoles);
            }
            
            // Save employee first
            Employee savedEmployee = employeeRepository.save(employee);
            logger.info("Employee saved with ID: {}", savedEmployee.getId());
            
            // Link user to employee and save
            user.setEmployee(savedEmployee);
            User savedUser = userRepository.save(user);
            logger.info("User saved with ID: {}, linked to employee ID: {}", savedUser.getId(), savedEmployee.getId());
            
            // Update employee with user reference
            savedEmployee.setUser(savedUser);
            employeeRepository.save(savedEmployee);
            logger.info("Employee updated with user reference: {}", savedEmployee.getId());
            
            logger.info("Employee and user saved successfully: {} {} (ID: {})", 
                savedEmployee.getName(), savedEmployee.getSurname(), savedEmployee.getId());
            return ResponseEntity.ok(savedEmployee);
        } catch (Exception e) {
            logger.error("Error creating employee: {}", e.getMessage(), e);
            if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                return ResponseEntity.badRequest().body("Работник с таким номером телефона уже существует");
            }
            return ResponseEntity.badRequest().body("Error creating employee: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> updateEmployee(@PathVariable Long id, @RequestBody Employee employeeDetails) {
        // Get current user
        org.springframework.security.core.Authentication authentication = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        
        final String currentUsername;
        if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
            org.springframework.security.core.userdetails.UserDetails userDetails = 
                (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal();
            currentUsername = userDetails.getUsername();
        } else {
            currentUsername = null;
        }
        
        return employeeRepository.findById(id)
                .map(employee -> {
                    // Check if this is the Administrator or admin user
                    if (employee.getUser() != null) {
                        String employeeLogin = employee.getUser().getLogin();
                        if ("Administrator".equals(employeeLogin)) {
                            // Only Administrator can edit Administrator
                            if (!"Administrator".equals(currentUsername)) {
                                return ResponseEntity.status(403).body("Нельзя редактировать пользователя Administrator");
                            }
                        } else if ("admin".equals(employeeLogin)) {
                            // Only Administrator or admin can edit admin
                            if (!"Administrator".equals(currentUsername) && !"admin".equals(currentUsername)) {
                                return ResponseEntity.status(403).body("Нельзя редактировать пользователя admin");
                            }
                        }
                    }
                    
                    // Update name/surname snapshots in work results if they changed
                    boolean nameChanged = !employee.getName().equals(employeeDetails.getName());
                    boolean surnameChanged = !employee.getSurname().equals(employeeDetails.getSurname());
                    
                    employee.setName(employeeDetails.getName());
                    employee.setSurname(employeeDetails.getSurname());
                    
                    if (nameChanged || surnameChanged) {
                        workResultRepository.updateEmployeeNameSnapshots(
                            employee.getId(), 
                            employeeDetails.getName(), 
                            employeeDetails.getSurname()
                        );
                    }
                    
                    employee.setDay(employeeDetails.getDay());
                    employee.setMonth(employeeDetails.getMonth());
                    employee.setYear(employeeDetails.getYear());
                    employee.setPhone(employeeDetails.getPhone());
                    employee.setCity(employeeDetails.getCity());
                    employee.setStreet(employeeDetails.getStreet());
                    employee.setHouse(employeeDetails.getHouse());
                    employee.setRoom(employeeDetails.getRoom());
                    
                    // Update active status based on checkbox
                    employee.setActive(employeeDetails.getActive());
                    
                    // Update login if provided and different from current
                    if (employeeDetails.getLogin() != null && !employeeDetails.getLogin().trim().isEmpty()) {
                        String newLogin = employeeDetails.getLogin().trim().toLowerCase();
                        String currentLogin = employee.getUser() != null ? employee.getUser().getLogin() : null;
                        
                        if (!newLogin.equals(currentLogin)) {
                            // Check if new login already exists
                            if (userRepository.existsByLogin(newLogin)) {
                                return ResponseEntity.badRequest().body("Пользователь с таким логином уже существует");
                            }
                            
                            // Update login in user entity
                            if (employee.getUser() != null) {
                                employee.getUser().setLogin(newLogin);
                                userRepository.save(employee.getUser());
                                logger.info("Login updated from {} to {}", currentLogin, newLogin);
                            }
                        }
                    }
                    
                    // Update roles if provided
                    logger.info("Roles from request: {}", employeeDetails.getRoles());
                    if (employeeDetails.getRoles() != null && !employeeDetails.getRoles().isEmpty()) {
                        if (employee.getUser() != null) {
                            logger.info("Assigning roles with inheritance: {}", employeeDetails.getRoles());
                            Set<Role> assignedRoles = assignRolesBasedOnAccess(employeeDetails.getRoles());
                            logger.info("Assigned roles with inheritance: {}", assignedRoles);
                            Long userId = employee.getUser().getId();
                            
                            userRepository.deleteUserRoles(userId);
                            for (Role role : assignedRoles) {
                                userRepository.insertUserRole(userId, role.name());
                            }
                            
                            logger.info("User roles saved successfully with inheritance: {}", assignedRoles);
                        } else {
                            logger.warn("Employee has no user entity, cannot update roles");
                        }
                    } else {
                        logger.warn("Roles are null or empty, skipping role update");
                    }
                    
                    // Update password only if provided
                    if (employeeDetails.getPassword() != null && !employeeDetails.getPassword().trim().isEmpty()) {
                        // Update user password
                        if (employee.getUser() != null) {
                            String hashedPassword = passwordEncoder.encode(employeeDetails.getPassword());
                            employee.getUser().setPassword(hashedPassword);
                            userRepository.save(employee.getUser());
                        }
                    }
                    
                    Employee updatedEmployee = employeeRepository.save(employee);
                    return ResponseEntity.ok().body(updatedEmployee);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        return employeeRepository.findById(id)
                .map(employee -> {
                    // Check if this is the Administrator or admin user
                    if (employee.getUser() != null) {
                        String employeeLogin = employee.getUser().getLogin();
                        if ("Administrator".equals(employeeLogin)) {
                            // Administrator cannot be deleted by anyone, including themselves
                            return ResponseEntity.status(403).<Void>build();
                        } else if ("admin".equals(employeeLogin)) {
                            // admin cannot be deleted by anyone except Administrator
                            org.springframework.security.core.Authentication authentication = 
                                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                            
                            if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
                                org.springframework.security.core.userdetails.UserDetails userDetails = 
                                    (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal();
                                
                                if (!"Administrator".equals(userDetails.getUsername())) {
                                    return ResponseEntity.status(403).<Void>build();
                                }
                            }
                        }
                    }
                    
                    employee.setActive(false);
                    employeeRepository.save(employee);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private Set<Role> assignRolesBasedOnAccess(Set<Role> selectedAccess) {
        Set<Role> assignedRoles = new HashSet<>();
        
        boolean hasAdministratorRole = selectedAccess.contains(Role.ADMINISTRATOR);
        boolean hasManagerRole = selectedAccess.contains(Role.MANAGER);
        boolean hasTechnologistRole = selectedAccess.contains(Role.TECHNOLOGIST);
        boolean hasMasterRole = selectedAccess.contains(Role.MASTER);
        boolean hasAccountantRole = selectedAccess.contains(Role.ACCOUNTANT);
        boolean hasEmployeeRole = selectedAccess.contains(Role.EMPLOYEE);
        
        // ADMINISTRATOR - все роли
        if (hasAdministratorRole) {
            assignedRoles.add(Role.ADMINISTRATOR);
            assignedRoles.add(Role.MANAGER);
            assignedRoles.add(Role.TECHNOLOGIST);
            assignedRoles.add(Role.MASTER);
            assignedRoles.add(Role.ACCOUNTANT);
            assignedRoles.add(Role.EMPLOYEE);
        }
        // MANAGER - MANAGER, TECHNOLOGIST, MASTER, ACCOUNTANT, EMPLOYEE
        else if (hasManagerRole) {
            assignedRoles.add(Role.MANAGER);
            assignedRoles.add(Role.TECHNOLOGIST);
            assignedRoles.add(Role.MASTER);
            assignedRoles.add(Role.ACCOUNTANT);
            assignedRoles.add(Role.EMPLOYEE);
        }
        // TECHNOLOGIST - TECHNOLOGIST, MASTER, EMPLOYEE
        else if (hasTechnologistRole) {
            assignedRoles.add(Role.TECHNOLOGIST);
            assignedRoles.add(Role.MASTER);
            assignedRoles.add(Role.EMPLOYEE);
        }
        // MASTER - MASTER, EMPLOYEE
        else if (hasMasterRole) {
            assignedRoles.add(Role.MASTER);
            assignedRoles.add(Role.EMPLOYEE);
        }
        // ACCOUNTANT - только ACCOUNTANT
        else if (hasAccountantRole) {
            assignedRoles.add(Role.ACCOUNTANT);
        }
        // EMPLOYEE - только EMPLOYEE
        else if (hasEmployeeRole) {
            assignedRoles.add(Role.EMPLOYEE);
        }
        
        // If no roles assigned, default to EMPLOYEE
        if (assignedRoles.isEmpty()) {
            assignedRoles.add(Role.EMPLOYEE);
        }
        
        return assignedRoles;
    }

    private List<Employee> filterAdministrator(List<Employee> employees) {
        org.springframework.security.core.Authentication authentication = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        
        String currentUsername = null;
        boolean isAdministrator = false;
        boolean isAdmin = false;
        
        if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
            org.springframework.security.core.userdetails.UserDetails userDetails = 
                (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal();
            
            currentUsername = userDetails.getUsername();
            isAdministrator = "Administrator".equals(currentUsername);
            isAdmin = "admin".equals(currentUsername);
        }
        
        final boolean finalIsAdministrator = isAdministrator;
        final boolean finalIsAdmin = isAdmin;
        final String finalCurrentUsername = currentUsername;
        return employees.stream()
            .filter(employee -> {
                if (employee.getUser() == null) {
                    return true;
                }
                String employeeLogin = employee.getUser().getLogin();
                // Administrator sees everyone
                if (finalIsAdministrator) {
                    return true;
                }
                // admin sees themselves and all workers except Administrator
                if (finalIsAdmin) {
                    return !"Administrator".equals(employeeLogin);
                }
                // Current user sees themselves
                if (employeeLogin.equals(finalCurrentUsername)) {
                    return true;
                }
                // Others don't see Administrator or admin
                return !"Administrator".equals(employeeLogin) && !"admin".equals(employeeLogin);
            })
            .toList();
    }
}
