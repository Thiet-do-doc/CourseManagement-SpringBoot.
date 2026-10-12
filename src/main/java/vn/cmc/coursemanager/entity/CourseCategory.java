package vn.cmc.coursemanager.entity;
import jakarta.persistence.*;
import org.hibernate.annotations.Nationalized;
import java.util.*;
import java.math.BigDecimal;
@Entity
@Table(name="CourseCategories")
public class CourseCategory {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="Id")
    private Long id;

    @Nationalized
    @Column(name="Name", nullable=false, length=120)
    private String name;
    @Nationalized
    @Column(name="Description", length=2000)
    private String description;
    @OneToMany(mappedBy="category")
    private List<Course> courses = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<Course> getCourses() { return courses; }
    public void setCourses(List<Course> courses) { this.courses = courses; }
}
