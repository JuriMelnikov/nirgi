package ee.jvm.nirgi_java.repository;

import ee.jvm.nirgi_java.classes.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    List<Order> findByYearAndMonthAndWeek(Integer year, Integer month, Integer week);
    
    List<Order> findByYear(Integer year);
    
    List<Order> findByYearAndMonth(Integer year, Integer month);
    
    Optional<Order> findByName(String name);
}
