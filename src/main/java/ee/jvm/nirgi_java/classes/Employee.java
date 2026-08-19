package ee.jvm.nirgi_java.classes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ee.jvm.nirgi_java.security.Role;
import ee.jvm.nirgi_java.security.User;
import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String surname;

    @Column(name = "birth_day")
    private Integer day;

    @Column(name = "birth_month")
    private Integer month;

    @Column(name = "birth_year")
    private Integer year;

    @Column(nullable = false, unique = true)
    private String phone;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String street;

    @Column(nullable = false)
    private String house;

    @Column
    private String room;

    @OneToOne(mappedBy = "employee", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private User user;

    @Transient
    private String password;

    @Transient
    private String login;

    @Transient
    private Set<Role> roles = new HashSet<>();

    @Column(name = "is_active")
    private Boolean active = true;

    public Employee() {
    }

    public Employee(Long id, String name, String surname, Integer day,
                  Integer month, Integer year, String phone,
                  String city, String street, String house,
                  String room) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.day = day;
        this.month = month;
        this.year = year;
        this.phone = phone;
        this.city = city;
        this.street = street;
        this.house = house;
        this.room = room;
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

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getHouse() {
        return house;
    }

    public void setHouse(String house) {
        this.house = house;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    // Custom setter to handle JSON deserialization from strings to Role enum
    @com.fasterxml.jackson.annotation.JsonSetter("roles")
    public void setRolesFromStrings(Set<String> roleStrings) {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Employee.class);
        logger.info("setRolesFromStrings called with: {}", roleStrings);
        this.roles = new HashSet<>();
        if (roleStrings != null) {
            for (String roleString : roleStrings) {
                try {
                    Role role = Role.valueOf(roleString);
                    this.roles.add(role);
                    logger.info("Added role: {}", role);
                } catch (IllegalArgumentException e) {
                    logger.warn("Invalid role string: {}", roleString);
                }
            }
        }
        logger.info("Final roles set: {}", this.roles);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Employee worker = (Employee) o;
        return Objects.equals(id, worker.id) && Objects.equals(name, worker.name) && Objects.equals(surname, worker.surname) && Objects.equals(day, worker.day) && Objects.equals(month, worker.month) && Objects.equals(year, worker.year) && Objects.equals(phone, worker.phone) && Objects.equals(city, worker.city) && Objects.equals(street, worker.street) && Objects.equals(house, worker.house) && Objects.equals(room, worker.room);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, surname, day, month, year,
                phone, city, street, house, room);
    }

    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", dey=" + day +
                ", month=" + month +
                ", year=" + year +
                ", phone='" + phone + '\'' +
                ", city='" + city + '\'' +
                ", street='" + street + '\'' +
                ", house='" + house + '\'' +
                ", room='" + room + '\'' +
                '}';
    }
}
