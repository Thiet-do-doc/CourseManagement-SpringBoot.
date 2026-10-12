package vn.cmc.coursemanager.entity;
import jakarta.persistence.*;
import java.util.*;
import java.math.BigDecimal;
@Entity
@Table(name="CourseImages")
public class CourseImage {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="Id")
    private Long id;

    @Column(name="ImageUrl", nullable=false, length=2000)
    private String imageUrl;
    @Column(name="IsThumbnail", nullable=false)
    private boolean thumbnail;
    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="CourseId", nullable=false)
    private Course course;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public boolean isThumbnail() { return thumbnail; }
    public void setThumbnail(boolean thumbnail) { this.thumbnail = thumbnail; }
    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
}
