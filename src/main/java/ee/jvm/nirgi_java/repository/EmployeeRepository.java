package ee.jvm.nirgi_java.repository;

import ee.jvm.nirgi_java.classes.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByPhone(String phone);

    List<Employee> findBySurname(String surname);

    List<Employee> findByCity(String city);

    @Query("SELECT w FROM Employee w WHERE w.name LIKE %:name%")
    List<Employee> findByNameContaining(@Param("name") String name);

    @Query("SELECT w FROM Employee w WHERE " +
           "(:name IS NULL OR w.name = :name) AND " +
           "(:surname IS NULL OR w.surname = :surname) AND " +
           "(:city IS NULL OR w.city = :city)")
    List<Employee> findByFilters(
            @Param("name") String name,
            @Param("surname") String surname,
            @Param("city") String city
    );

    List<Employee> findByActiveTrue();
}
