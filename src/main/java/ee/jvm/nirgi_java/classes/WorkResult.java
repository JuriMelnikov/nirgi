package ee.jvm.nirgi_java.classes;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "work_results")
public class WorkResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_list_id", nullable = false)
    private ModelList modelList;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_list_id")
    private SectionList sectionList;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "techmap_id")
    private Techmap techmap;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(nullable = false)
    private Integer year;
    
    @Column(nullable = false)
    private Integer month;
    
    @Column(nullable = false)
    private Integer week;

    @Column(name = "employee_name_snapshot")
    private String employeeNameSnapshot;

    @Column(name = "employee_surname_snapshot")
    private String employeeSurnameSnapshot;

    @Column(name = "order_name_snapshot", updatable = false)
    private String orderNameSnapshot;

    @Column(name = "order_year_snapshot", updatable = false)
    private Integer orderYearSnapshot;

    @Column(name = "order_month_snapshot", updatable = false)
    private Integer orderMonthSnapshot;

    @Column(name = "order_week_snapshot", updatable = false)
    private Integer orderWeekSnapshot;

    @Column(name = "model_list_name_snapshot", updatable = false)
    private String modelListNameSnapshot;

    @Column(name = "section_list_name_snapshot", updatable = false)
    private String sectionListNameSnapshot;

    @Column(name = "techmap_serial_snapshot", updatable = false)
    private String techmapSerialSnapshot;

    @Column(name = "techmap_descriptor_snapshot", updatable = false)
    private String techmapDescriptorSnapshot;

    @Column(name = "techmap_time_snapshot", updatable = false)
    private String techmapTimeSnapshot;

    @Column(name = "techmap_price_snapshot", updatable = false)
    private String techmapPriceSnapshot;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public WorkResult() {
    }

    public WorkResult(Employee employee, Order order, ModelList modelList,
                     SectionList sectionList, Techmap techmap, Integer quantity,
                     Integer year, Integer month, Integer week) {
        this.employee = employee;
        this.order = order;
        this.modelList = modelList;
        this.sectionList = sectionList;
        this.techmap = techmap;
        this.quantity = quantity;
        this.year = year;
        this.month = month;
        this.week = week;
        this.createdAt = LocalDateTime.now();

        this.employeeNameSnapshot = employee != null ? employee.getName() : null;
        this.employeeSurnameSnapshot = employee != null ? employee.getSurname() : null;
        this.orderNameSnapshot = order != null ? order.getName() : null;
        this.orderYearSnapshot = order != null ? order.getYear() : null;
        this.orderMonthSnapshot = order != null ? order.getMonth() : null;
        this.orderWeekSnapshot = order != null ? order.getWeek() : null;
        this.modelListNameSnapshot = modelList != null ? modelList.getName() : null;
        this.sectionListNameSnapshot = sectionList != null ? sectionList.getName() : null;
        this.techmapSerialSnapshot = techmap != null ? techmap.getSerial() : null;
        this.techmapDescriptorSnapshot = techmap != null ? techmap.getDescriptor() : null;
        this.techmapTimeSnapshot = techmap != null ? techmap.getTime() : null;
        this.techmapPriceSnapshot = techmap != null ? techmap.getPrice() : null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public ModelList getModelList() {
        return modelList;
    }

    public void setModelList(ModelList modelList) {
        this.modelList = modelList;
    }

    public SectionList getSectionList() {
        return sectionList;
    }

    public void setSectionList(SectionList sectionList) {
        this.sectionList = sectionList;
    }

    public Techmap getTechmap() {
        return techmap;
    }

    public void setTechmap(Techmap techmap) {
        this.techmap = techmap;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getWeek() {
        return week;
    }

    public void setWeek(Integer week) {
        this.week = week;
    }

    public String getEmployeeNameSnapshot() {
        return employeeNameSnapshot;
    }

    public String getEmployeeSurnameSnapshot() {
        return employeeSurnameSnapshot;
    }

    public String getOrderNameSnapshot() {
        return orderNameSnapshot;
    }

    public Integer getOrderYearSnapshot() {
        return orderYearSnapshot;
    }

    public Integer getOrderMonthSnapshot() {
        return orderMonthSnapshot;
    }

    public Integer getOrderWeekSnapshot() {
        return orderWeekSnapshot;
    }

    public String getModelListNameSnapshot() {
        return modelListNameSnapshot;
    }

    public String getSectionListNameSnapshot() {
        return sectionListNameSnapshot;
    }

    public String getTechmapSerialSnapshot() {
        return techmapSerialSnapshot;
    }

    public String getTechmapDescriptorSnapshot() {
        return techmapDescriptorSnapshot;
    }

    public String getTechmapTimeSnapshot() {
        return techmapTimeSnapshot;
    }

    public String getTechmapPriceSnapshot() {
        return techmapPriceSnapshot;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        WorkResult that = (WorkResult) o;
        return Objects.equals(id, that.id) && 
               Objects.equals(employee, that.employee) && 
               Objects.equals(order, that.order) && 
               Objects.equals(modelList, that.modelList) && 
               Objects.equals(sectionList, that.sectionList) && 
               Objects.equals(techmap, that.techmap) && 
               Objects.equals(quantity, that.quantity) && 
               Objects.equals(year, that.year) && 
               Objects.equals(month, that.month) && 
               Objects.equals(week, that.week);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, employee, order, modelList, sectionList, techmap, quantity, year, month, week);
    }

    @Override
    public String toString() {
        return "WorkResult{" +
                "id=" + id +
                ", employee=" + employee +
                ", order=" + order +
                ", modelList=" + modelList +
                ", sectionList=" + sectionList +
                ", techmap=" + techmap +
                ", quantity=" + quantity +
                ", year=" + year +
                ", month=" + month +
                ", week=" + week +
                '}';
    }
}
