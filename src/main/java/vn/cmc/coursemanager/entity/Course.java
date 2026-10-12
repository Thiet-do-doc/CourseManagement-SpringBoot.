package vn.cmc.coursemanager.entity;
import jakarta.persistence.*;
import org.hibernate.annotations.Nationalized;
import java.util.*;
import java.math.BigDecimal;
@Entity
@Table(name="Courses", uniqueConstraints=@UniqueConstraint(name="UK_Course_Category_Name", columnNames={"CourseCategoryId", "Name"}))
public class Course {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="Id")
    private Long id;

    @Nationalized
    @Column(name="Name", nullable=false, length=150)
    private String name;
    @Column(name="TuitionFee", nullable=false, precision=18, scale=2)
    private BigDecimal tuitionFee;
    @Column(name="Duration", nullable=false)
    private Integer duration;
    @Column(name="IsActive", nullable=false)
    private boolean active;
    @Nationalized
    @Column(name="Description", length=4000)
    private String description;
    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="CourseCategoryId", nullable=false)
    private CourseCategory category;
    @OneToMany(mappedBy="course", cascade=CascadeType.ALL, orphanRemoval=true)
    @OrderBy("id ASC")
    private List<CourseImage> images = new ArrayList<>();
    // Chi tinh luc hien thi; KHONG tao cot DurationType trong database.
    @Transient
    public String getDurationType() { return duration <= 30 ? "Ngắn hạn" : "Dài hạn"; }
    @Transient
    public CourseImage getThumbnail() {
        return images.stream().filter(CourseImage::isThumbnail).findFirst().orElse(null);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getTuitionFee() { return tuitionFee; }
    public void setTuitionFee(BigDecimal tuitionFee) { this.tuitionFee = tuitionFee; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public CourseCategory getCategory() { return category; }
    public void setCategory(CourseCategory category) { this.category = category; }
    public List<CourseImage> getImages() { return images; }
    public void setImages(List<CourseImage> images) { this.images = images; }
}
