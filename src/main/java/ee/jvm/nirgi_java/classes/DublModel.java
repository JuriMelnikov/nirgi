package ee.jvm.nirgi_java.classes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "dubl_models")
public class DublModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dubl_order_id", nullable = false)
    @JsonIgnore
    private DublOrder dublOrder;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "model_list_id", nullable = false)
    private ModelList modelList;
    
    @Column(nullable = false)
    private Integer totalCount;
    
    @Column(nullable = false)
    private Integer completedInOriginalWeek;
    
    @Column(nullable = false)
    private Integer remainingCount;

    public DublModel() {
    }

    public DublModel(Long id, DublOrder dublOrder, ModelList modelList, Integer totalCount, Integer completedInOriginalWeek, Integer remainingCount) {
        this.id = id;
        this.dublOrder = dublOrder;
        this.modelList = modelList;
        this.totalCount = totalCount;
        this.completedInOriginalWeek = completedInOriginalWeek;
        this.remainingCount = remainingCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DublOrder getDublOrder() {
        return dublOrder;
    }

    public void setDublOrder(DublOrder dublOrder) {
        this.dublOrder = dublOrder;
    }

    public ModelList getModelList() {
        return modelList;
    }

    public void setModelList(ModelList modelList) {
        this.modelList = modelList;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getCompletedInOriginalWeek() {
        return completedInOriginalWeek;
    }

    public void setCompletedInOriginalWeek(Integer completedInOriginalWeek) {
        this.completedInOriginalWeek = completedInOriginalWeek;
    }

    public Integer getRemainingCount() {
        return remainingCount;
    }

    public void setRemainingCount(Integer remainingCount) {
        this.remainingCount = remainingCount;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DublModel dublModel = (DublModel) o;
        return Objects.equals(id, dublModel.id) && Objects.equals(dublOrder, dublModel.dublOrder) && 
               Objects.equals(modelList, dublModel.modelList) && Objects.equals(totalCount, dublModel.totalCount) && 
               Objects.equals(completedInOriginalWeek, dublModel.completedInOriginalWeek) && 
               Objects.equals(remainingCount, dublModel.remainingCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, dublOrder, modelList, totalCount, completedInOriginalWeek, remainingCount);
    }

    @Override
    public String toString() {
        return "DublModel{" +
                "id=" + id +
                ", dublOrder=" + dublOrder +
                ", modelList=" + modelList +
                ", totalCount=" + totalCount +
                ", completedInOriginalWeek=" + completedInOriginalWeek +
                ", remainingCount=" + remainingCount +
                '}';
    }
}
