package ee.jvm.nirgi_java.classes;

import java.util.Objects;

public class Appointment {
    private Long id;
    private String name;
    private Boolean del;

    public Appointment() {
    }

    public Appointment(Long id, String name, Boolean del) {
        this.id = id;
        this.name = name;
        this.del = del;
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

    public Boolean getDel() {
        return del;
    }

    public void setDel(Boolean del) {
        this.del = del;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Appointment that = (Appointment) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(del, that.del);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, del);
    }

    @Override
    public String toString() {
        return "Appointment{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", del=" + del +
                '}';
    }
}
