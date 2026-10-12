package vn.cmc.coursemanager.repository;
import vn.cmc.coursemanager.entity.CourseImage;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;
public interface CourseImageRepository extends JpaRepository<CourseImage, Long> {
    Optional<CourseImage> findByIdAndCourseId(Long id, Long courseId);
    long countByCourseIdAndThumbnailTrue(Long courseId);
    @Modifying(flushAutomatically=true, clearAutomatically=true)
    @Query("update CourseImage i set i.thumbnail = false where i.course.id = :courseId and i.thumbnail = true")
    int clearThumbnail(@Param("courseId") Long courseId);
}
