package vn.cmc.coursemanager.repository;
import vn.cmc.coursemanager.entity.CourseCategory;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;
public interface CourseCategoryRepository extends JpaRepository<CourseCategory, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CourseCategory c where c.id = :id")
    Optional<CourseCategory> findLockedById(@Param("id") Long id);
}
