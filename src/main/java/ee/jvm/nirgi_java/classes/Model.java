package ee.jvm.nirgi_java.classes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "models")
public class Model {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "model_list_id")
    private ModelList modelList;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    @JsonIgnore
    private Order order;
    
    @Column(nullable = false)
    private Integer count;

    public Model() {
    }

    public Model(Long id, ModelList modelList, Order order, Integer count) {
        this.id = id;
        this.modelList = modelList;
        this.order = order;
        this.count = count;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ModelList getModelList() {
        return modelList;
    }

    public void setModelList(ModelList modelList) {
        this.modelList = modelList;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Model model = (Model) o;
        return Objects.equals(id, model.id) && Objects.equals(modelList, model.modelList) && Objects.equals(order, model.order) && Objects.equals(count, model.count);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, modelList, order, count);
    }

    @Override
    public String toString() {
        return "Model{" +
                "id=" + id +
                ", modelList=" + modelList +
                ", order=" + order +
                ", count=" + count +
                '}';
    }
}

