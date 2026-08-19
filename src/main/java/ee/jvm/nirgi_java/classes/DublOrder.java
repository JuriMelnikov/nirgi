package ee.jvm.nirgi_java.classes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "dubl_orders")
public class DublOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    private Order order;
    
    @Column(nullable = false)
    private Integer originalYear;
    
    @Column(nullable = false)
    private Integer originalMonth;
    
    @Column(nullable = false)
    private Integer originalWeek;
    
    @Column(nullable = false)
    private Integer targetYear;
    
    @Column(nullable = false)
    private Integer targetMonth;
    
    @Column(nullable = false)
    private Integer targetWeek;

    @OneToMany(mappedBy = "dublOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<DublModel> dublModels = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_order_id")
    private Order createdOrder;

    public DublOrder() {
    }

    public DublOrder(Long id, Order order, Integer originalYear, Integer originalMonth, Integer originalWeek, 
                     Integer targetYear, Integer targetMonth, Integer targetWeek) {
        this.id = id;
        this.order = order;
        this.originalYear = originalYear;
        this.originalMonth = originalMonth;
        this.originalWeek = originalWeek;
        this.targetYear = targetYear;
        this.targetMonth = targetMonth;
        this.targetWeek = targetWeek;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Integer getOriginalYear() {
        return originalYear;
    }

    public void setOriginalYear(Integer originalYear) {
        this.originalYear = originalYear;
    }

    public Integer getOriginalMonth() {
        return originalMonth;
    }

    public void setOriginalMonth(Integer originalMonth) {
        this.originalMonth = originalMonth;
    }

    public Integer getOriginalWeek() {
        return originalWeek;
    }

    public void setOriginalWeek(Integer originalWeek) {
        this.originalWeek = originalWeek;
    }

    public Integer getTargetYear() {
        return targetYear;
    }

    public void setTargetYear(Integer targetYear) {
        this.targetYear = targetYear;
    }

    public Integer getTargetMonth() {
        return targetMonth;
    }

    public void setTargetMonth(Integer targetMonth) {
        this.targetMonth = targetMonth;
    }

    public Integer getTargetWeek() {
        return targetWeek;
    }

    public void setTargetWeek(Integer targetWeek) {
        this.targetWeek = targetWeek;
    }

    public List<DublModel> getDublModels() {
        return dublModels;
    }

    public void setDublModels(List<DublModel> dublModels) {
        this.dublModels = dublModels;
    }

    public void addDublModel(DublModel dublModel) {
        dublModels.add(dublModel);
        dublModel.setDublOrder(this);
    }

    public void removeDublModel(DublModel dublModel) {
        dublModels.remove(dublModel);
        dublModel.setDublOrder(null);
    }

    public Order getCreatedOrder() {
        return createdOrder;
    }

    public void setCreatedOrder(Order createdOrder) {
        this.createdOrder = createdOrder;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DublOrder dublOrder = (DublOrder) o;
        return Objects.equals(id, dublOrder.id) && Objects.equals(order, dublOrder.order) && 
               Objects.equals(originalYear, dublOrder.originalYear) && Objects.equals(originalMonth, dublOrder.originalMonth) && 
               Objects.equals(originalWeek, dublOrder.originalWeek) && Objects.equals(targetYear, dublOrder.targetYear) && 
               Objects.equals(targetMonth, dublOrder.targetMonth) && Objects.equals(targetWeek, dublOrder.targetWeek);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, order, originalYear, originalMonth, originalWeek, targetYear, targetMonth, targetWeek);
    }

    @Override
    public String toString() {
        return "DublOrder{" +
                "id=" + id +
                ", order=" + order +
                ", originalYear=" + originalYear +
                ", originalMonth=" + originalMonth +
                ", originalWeek=" + originalWeek +
                ", targetYear=" + targetYear +
                ", targetMonth=" + targetMonth +
                ", targetWeek=" + targetWeek +
                '}';
    }
}
