package vn.cmc.coursemanager.repository;
import vn.cmc.coursemanager.entity.Course;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Sort;
import jakarta.persistence.LockModeType;
import java.util.*;
public interface CourseRepository extends JpaRepository<Course, Long>, JpaSpecificationExecutor<Course> {
    boolean existsByCategoryId(Long categoryId);
    boolean existsByCategoryIdAndNameIgnoreCase(Long categoryId, String name);
    boolean existsByCategoryIdAndNameIgnoreCaseAndIdNot(Long categoryId, String name, Long id);
    @Override @EntityGraph(attributePaths={"category", "images"})
    List<Course> findAll(Specification<Course> spec, Sort sort);
    @Query("select distinct c from Course c left join fetch c.images join fetch c.category where c.id = :id")
    Optional<Course> findDetailById(@Param("id") Long id);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Course c where c.id = :id")
    Optional<Course> findLockedById(@Param("id") Long id);
}
