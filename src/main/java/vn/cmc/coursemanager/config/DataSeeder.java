package vn.cmc.coursemanager.config;
import vn.cmc.coursemanager.entity.*;
import vn.cmc.coursemanager.repository.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
@Component
@ConditionalOnProperty(name="app.seed.enabled", havingValue="true", matchIfMissing=true)
public class DataSeeder implements ApplicationRunner {
    private final CourseRepository courses; private final CourseCategoryRepository categories; private final CourseImageRepository images;
    @Value("${app.demo-image-base-url}") private String imageBase;
    public DataSeeder(CourseRepository courses, CourseCategoryRepository categories, CourseImageRepository images) {
        this.courses=courses; this.categories=categories; this.images=images;
    }
    @Override @Transactional
    public void run(ApplicationArguments args) {
        if (categories.count()>0 || courses.count()>0) return;
        CourseCategory web=category("Lập trình Web", "Xây dựng website và ứng dụng web.");
        CourseCategory data=category("Dữ liệu & AI", "Phân tích dữ liệu và học máy.");
        CourseCategory embedded=category("Nhúng & IoT", "Lập trình thiết bị và hệ thống kết nối.");
        Course spring=course("Java Spring Boot", "2500000", 45, true, web, "Xây dựng ứng dụng MVC với Spring Boot, Thymeleaf và JPA.");
        course("HTML CSS JavaScript", "1200000", 30, true, web, "Nền tảng xây dựng giao diện website.");
        course("Python phân tích dữ liệu", "1800000", 24, true, data, "Làm sạch, xử lý và trực quan hóa dữ liệu.");
        Course ai=course("Machine Learning căn bản", "3200000", 60, false, data, "Khóa học tạm ngừng tuyển sinh để cập nhật nội dung.");
        course("ESP32 và IoT", "2200000", 36, true, embedded, "Cảm biến, ESP32 và kết nối thiết bị với server.");
        image(spring,"web.svg",true); image(spring,"spring.svg",false); image(ai,"data.svg",true);
    }
    private CourseCategory category(String name,String description) {
        CourseCategory c=new CourseCategory();c.setName(name);c.setDescription(description);return categories.save(c);
    }
    private Course course(String name,String fee,int duration,boolean active,CourseCategory cat,String description) {
        Course c=new Course();c.setName(name);c.setTuitionFee(new BigDecimal(fee));c.setDuration(duration);c.setActive(active);c.setCategory(cat);c.setDescription(description);return courses.save(c);
    }
    private void image(Course c,String file,boolean thumbnail) {
        CourseImage i=new CourseImage();i.setCourse(c);i.setImageUrl(imageBase+"/"+file);i.setThumbnail(thumbnail);images.save(i);
    }
}
