package ee.jvm.nirgi_java.repository;

import ee.jvm.nirgi_java.classes.DublOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DublOrderRepository extends JpaRepository<DublOrder, Long> {
    
    List<DublOrder> findByTargetYearAndTargetMonthAndTargetWeek(Integer targetYear, Integer targetMonth, Integer targetWeek);
    
    List<DublOrder> findByOriginalYearAndOriginalMonthAndOriginalWeek(Integer originalYear, Integer originalMonth, Integer originalWeek);
    
    List<DublOrder> findByOrderId(Long orderId);
    
    Optional<DublOrder> findByOrderIdAndTargetYearAndTargetMonthAndTargetWeek(Long orderId, Integer targetYear, Integer targetMonth, Integer targetWeek);
    
    @Query("SELECT do FROM DublOrder do WHERE do.order.name = :orderName AND do.originalYear = :year AND do.originalMonth = :month AND do.originalWeek = :week")
    Optional<DublOrder> findByOrderNameAndOriginalWeek(@Param("orderName") String orderName, @Param("year") Integer year, @Param("month") Integer month, @Param("week") Integer week);
}
