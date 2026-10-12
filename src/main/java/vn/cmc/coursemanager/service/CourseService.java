package vn.cmc.coursemanager.service;
import vn.cmc.coursemanager.dto.*;
import vn.cmc.coursemanager.entity.*;
import vn.cmc.coursemanager.exception.*;
import vn.cmc.coursemanager.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Sort;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.net.URI;
import java.util.*;

@Service
@Transactional(readOnly=true)
public class CourseService {
    private final CourseRepository courses;
    private final CourseCategoryRepository categories;
    private final CourseImageRepository images;
    public CourseService(CourseRepository courses, CourseCategoryRepository categories, CourseImageRepository images) {
        this.courses=courses; this.categories=categories; this.images=images;
    }
    public Course detail(Long id) {
        return courses.findDetailById(id).orElseThrow(() -> new NotFoundException("Không tìm thấy khóa học có ID " + id + "."));
    }
    private Course lockCourse(Long id) {
        return courses.findLockedById(id).orElseThrow(() -> new NotFoundException("Khóa học không tồn tại."));
    }
    public List<Course> search(CourseFilter filter) {
        if (!filter.isFeeRangeValid()) throw new BusinessException("maxFee", "Học phí tối thiểu không được lớn hơn học phí tối đa.");
        if (filter.getCategoryId()!=null && !categories.existsById(filter.getCategoryId()))
            throw new BusinessException("categoryId", "Loại khóa học không tồn tại.");
        // Criteria API tao WHERE ... AND ... trong SQL. Khong findAll() roi loc bang Java.
        Specification<Course> spec = (root, query, cb) -> {
            List<Predicate> conditions=new ArrayList<>();
            if (filter.getKeyword()!=null && !filter.getKeyword().isBlank()) {
                String word=filter.getKeyword().trim().toLowerCase(Locale.ROOT)
                    .replace("!", "!!").replace("%", "!%").replace("_", "!_");
                conditions.add(cb.like(cb.lower(root.get("name")), "%"+word+"%", '!'));
            }
            if (filter.getCategoryId()!=null) conditions.add(cb.equal(root.get("category").get("id"), filter.getCategoryId()));
            if (filter.getActive()!=null) conditions.add(cb.equal(root.get("active"), filter.getActive()));
            if (filter.getMinFee()!=null) conditions.add(cb.greaterThanOrEqualTo(root.<BigDecimal>get("tuitionFee"), filter.getMinFee()));
            if (filter.getMaxFee()!=null) conditions.add(cb.lessThanOrEqualTo(root.<BigDecimal>get("tuitionFee"), filter.getMaxFee()));
            return cb.and(conditions.toArray(new Predicate[0]));
        };
        Sort sort=switch (filter.getSort()==null ? "feeAsc" : filter.getSort()) {
            case "feeDesc" -> Sort.by(Sort.Direction.DESC, "tuitionFee");
            case "durationAsc" -> Sort.by("duration");
            default -> Sort.by("tuitionFee");
        };
        return courses.findAll(spec, sort.and(Sort.by("id")));
    }
    public CourseForm form(Long id) {
        Course c=detail(id); CourseForm f=new CourseForm();
        f.setName(c.getName()); f.setTuitionFee(c.getTuitionFee()); f.setDuration(c.getDuration());
        f.setActive(c.isActive()); f.setDescription(c.getDescription()); f.setCategoryId(c.getCategory().getId());
        return f;
    }
    @Transactional
    public Long save(Long id, CourseForm f) {
        Course c=id==null ? new Course() : lockCourse(id);
        // Khoa dong category de 2 request cung loai khong vuot qua kiem tra trung ten.
        CourseCategory category=categories.findLockedById(f.getCategoryId())
            .orElseThrow(() -> new BusinessException("categoryId", "Loại khóa học không tồn tại."));
        String name=f.getName().trim();
        boolean duplicate=id==null ? courses.existsByCategoryIdAndNameIgnoreCase(category.getId(), name)
            : courses.existsByCategoryIdAndNameIgnoreCaseAndIdNot(category.getId(), name, id);
        if (duplicate) throw new BusinessException("name", "Tên khóa học đã tồn tại trong loại này.");
        c.setName(name); c.setTuitionFee(f.getTuitionFee()); c.setDuration(f.getDuration());
        c.setActive(f.isActive()); c.setDescription(f.getDescription()); c.setCategory(category);
        return courses.saveAndFlush(c).getId();
    }
    @Transactional
    public void delete(Long id) { courses.delete(lockCourse(id)); courses.flush(); }
    @Transactional
    public void addImage(Long courseId, ImageForm f) {
        Course course=lockCourse(courseId);
        String url=f.getImageUrl().trim();
        try {
            URI uri=URI.create(url);
            if (uri.getHost()==null || uri.getUserInfo()!=null ||
                !("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))) throw new IllegalArgumentException();
        } catch (IllegalArgumentException ex) { throw new BusinessException("imageUrl", "URL phải là địa chỉ http:// hoặc https:// hợp lệ."); }
        if (f.isThumbnail()) images.clearThumbnail(courseId);
        CourseImage image=new CourseImage(); image.setImageUrl(url); image.setThumbnail(f.isThumbnail());
        image.setCourse(course); images.saveAndFlush(image);
    }
    @Transactional
    public void setThumbnail(Long courseId, Long imageId) {
        lockCourse(courseId); // Cac lan doi anh cung khoa hoc duoc thuc hien lan luot.
        requireImage(courseId, imageId); // Kiem tra truoc khi bo chon anh cu.
        images.clearThumbnail(courseId);
        CourseImage image=requireImage(courseId, imageId);
        image.setThumbnail(true); images.saveAndFlush(image);
    }
    @Transactional
    public void deleteImage(Long courseId, Long imageId) {
        lockCourse(courseId); images.delete(requireImage(courseId,imageId)); images.flush();
    }
    private CourseImage requireImage(Long courseId, Long imageId) {
        return images.findByIdAndCourseId(imageId, courseId)
            .orElseThrow(() -> new NotFoundException("Ảnh không tồn tại hoặc không thuộc khóa học này."));
    }
}
