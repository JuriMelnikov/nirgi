package ee.jvm.nirgi_java.repository;

import ee.jvm.nirgi_java.classes.WorkResult;
import ee.jvm.nirgi_java.dto.SalaryRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkResultRepository extends JpaRepository<WorkResult, Long> {

    List<WorkResult> findByEmployeeIdAndYearAndMonthAndWeek(Long employeeId, Integer year, Integer month, Integer week);

    List<WorkResult> findByEmployeeIdAndYearAndMonth(Long employeeId, Integer year, Integer month);

    List<WorkResult> findByOrderIdAndYearAndMonthAndWeek(Long orderId, Integer year, Integer month, Integer week);

    List<WorkResult> findByOrderIdAndYearAndMonth(Long orderId, Integer year, Integer month);

    List<WorkResult> findByEmployeeIdAndOrderId(Long employeeId, Long orderId);

    List<WorkResult> findByEmployeeIdAndOrderIdAndYearAndMonthAndWeek(Long employeeId, Long orderId, Integer year, Integer month, Integer week);

    long countByTechmapId(Long techmapId);

    @Query("SELECT SUM(wr.quantity) FROM WorkResult wr WHERE wr.employee.id = :employeeId AND wr.order.id = :orderId AND wr.modelList.id = :modelListId AND wr.sectionList.id = :sectionListId AND wr.techmap.id = :techmapId AND wr.year = :year AND wr.month = :month AND wr.week = :week")
    Integer sumQuantityByEmployeeOrderModelSectionTechmapAndDate(
            @Param("employeeId") Long employeeId,
            @Param("orderId") Long orderId,
            @Param("modelListId") Long modelListId,
            @Param("sectionListId") Long sectionListId,
            @Param("techmapId") Long techmapId,
            @Param("year") Integer year,
            @Param("month") Integer month,
            @Param("week") Integer week
    );

    @Query("SELECT SUM(wr.quantity) FROM WorkResult wr WHERE wr.employee.id = :employeeId AND wr.order.id = :orderId AND wr.modelList.id = :modelListId AND wr.sectionList.id = :sectionListId AND wr.techmap.id = :techmapId")
    Integer sumQuantityByEmployeeOrderModelSectionTechmap(
            @Param("employeeId") Long employeeId,
            @Param("orderId") Long orderId,
            @Param("modelListId") Long modelListId,
            @Param("sectionListId") Long sectionListId,
            @Param("techmapId") Long techmapId
    );

    @Query("SELECT COALESCE(SUM(wr.quantity), 0) FROM WorkResult wr WHERE wr.employee.id = :employeeId AND wr.modelList.id = :modelListId AND wr.order.id = :orderId AND (:sectionListId IS NULL OR wr.sectionList.id = :sectionListId) AND (:techmapId IS NULL OR wr.techmap.id = :techmapId)")
    Integer sumQuantityByEmployeeOrderModelSectionTechmapIncludingTransferred(
            @Param("employeeId") Long employeeId,
            @Param("orderId") Long orderId,
            @Param("modelListId") Long modelListId,
            @Param("sectionListId") Long sectionListId,
            @Param("techmapId") Long techmapId
    );

    @Query("SELECT new ee.jvm.nirgi_java.dto.SalaryRecord(" +
           "wr.employee.id, " +
           "wr.employeeNameSnapshot, " +
           "wr.employeeSurnameSnapshot, " +
           "SUM(wr.quantity * CAST(wr.techmapTimeSnapshot AS double)), " +
           "SUM(wr.quantity * CAST(wr.techmapPriceSnapshot AS double) / 100.0)" +
           ") FROM WorkResult wr " +
           "WHERE wr.year = :year AND wr.month = :month " +
           "GROUP BY wr.employee.id, wr.employeeNameSnapshot, wr.employeeSurnameSnapshot")
    List<SalaryRecord> findSalaryRecordsByMonthAndYear(@Param("year") Integer year, @Param("month") Integer month);

    @Modifying
    @Query("UPDATE WorkResult wr SET wr.employeeNameSnapshot = :name, wr.employeeSurnameSnapshot = :surname WHERE wr.employee.id = :employeeId")
    void updateEmployeeNameSnapshots(@Param("employeeId") Long employeeId, @Param("name") String name, @Param("surname") String surname);
}
