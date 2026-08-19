package ee.jvm.nirgi_java.classes;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "section_lists")
public class SectionList{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;

    public SectionList() {
    }

    public SectionList(Long id, String name) {
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
        SectionList that = (SectionList) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "SectionList{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
