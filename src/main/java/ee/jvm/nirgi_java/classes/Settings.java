package ee.jvm.nirgi_java.classes;

import jakarta.persistence.*;

@Entity
@Table(name = "settings")
public class Settings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stop_day", nullable = false)
    private Integer stopDay;

    public Settings() {
    }

    public Settings(Integer stopDay) {
        this.stopDay = stopDay;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getStopDay() {
        return stopDay;
    }

    public void setStopDay(Integer stopDay) {
        this.stopDay = stopDay;
    }
}
