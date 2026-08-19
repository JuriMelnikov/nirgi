package ee.jvm.nirgi_java.classes;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "model_list")
public class ModelList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;

    public ModelList() {
    }

    public ModelList(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ModelList modelList = (ModelList) o;
        return Objects.equals(id, modelList.id) && Objects.equals(name, modelList.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "ModelList{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
