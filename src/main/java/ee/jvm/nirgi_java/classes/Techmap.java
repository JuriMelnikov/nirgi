package ee.jvm.nirgi_java.classes;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "techmaps")
public class Techmap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String serial;
    
    @Column(nullable = false)
    private String descriptor;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_list_id")
    private ModelList modelList;
    
    @Column(nullable = false)
    private String time;
    
    @Column(nullable = false)
    private String price;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_list_id")
    private SectionList sectionList;

    public Techmap() {
    }

    public Techmap(Long id, String serial, String descriptor, ModelList modelList, String time, String price, SectionList sectionList) {
        this.id = id;
        this.serial = serial;
        this.descriptor = descriptor;
        this.modelList = modelList;
        this.time = time;
        this.price = price;
        this.sectionList = sectionList;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(String descriptor) {
        this.descriptor = descriptor;
    }

    public ModelList getModelList() {
        return modelList;
    }

    public void setModelList(ModelList modelList) {
        this.modelList = modelList;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public SectionList getSectionList() {
        return sectionList;
    }

    public void setSectionList(SectionList sectionList) {
        this.sectionList = sectionList;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Techmap techmap = (Techmap) o;
        return Objects.equals(id, techmap.id) && Objects.equals(serial, techmap.serial) && Objects.equals(descriptor, techmap.descriptor) && Objects.equals(modelList, techmap.modelList) && Objects.equals(time, techmap.time) && Objects.equals(price, techmap.price) && Objects.equals(sectionList, techmap.sectionList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, serial, descriptor, modelList, time, price, sectionList);
    }

    @Override
    public String toString() {
        return "Techmap{" +
                "id=" + id +
                ", serial='" + serial + '\'' +
                ", descriptor='" + descriptor + '\'' +
                ", modelList=" + modelList +
                ", time='" + time + '\'' +
                ", price='" + price + '\'' +
                ", sectionList=" + sectionList +
                '}';
    }
}
