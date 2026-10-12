package vn.cmc.coursemanager;
import vn.cmc.coursemanager.dto.*;
import vn.cmc.coursemanager.entity.*;
import vn.cmc.coursemanager.exception.*;
import vn.cmc.coursemanager.repository.*;
import vn.cmc.coursemanager.service.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:course_tests;DB_CLOSE_DELAY=-1;IGNORECASE=TRUE", "spring.jpa.hibernate.ddl-auto=create-drop", "app.seed.enabled=false"})
@ActiveProfiles("h2") @AutoConfigureMockMvc
class CourseManagementIntegrationTest {
    @Autowired MockMvc mvc; @Autowired CourseService service; @Autowired CategoryService categoryService;
    @Autowired CourseRepository courses; @Autowired CourseCategoryRepository categories; @Autowired CourseImageRepository images;
    @Autowired JdbcTemplate jdbc;
    Long webId,otherId,springId,inactiveId;
    @BeforeEach void setup() {
        images.deleteAll(); courses.deleteAll(); categories.deleteAll();
        CourseCategory a=new CourseCategory();a.setName("Web");webId=categories.saveAndFlush(a).getId();
        CourseCategory b=new CourseCategory();b.setName("AI");otherId=categories.saveAndFlush(b).getId();
        springId=service.save(null,form("Java Spring",webId,"2500000",45,true));
        service.save(null,form("Java Basic",webId,"1000000",30,true));
        inactiveId=service.save(null,form("Java Legacy",webId,"2300000",20,false));
        service.save(null,form("Java Data",otherId,"2400000",60,true));
    }
    CourseForm form(String name,Long category,String fee,int duration,boolean active) {
        CourseForm f=new CourseForm();f.setName(name);f.setCategoryId(category);f.setTuitionFee(new BigDecimal(fee));f.setDuration(duration);f.setActive(active);return f;
    }
    ImageForm image(String url,boolean thumb) { ImageForm f=new ImageForm();f.setImageUrl(url);f.setThumbnail(thumb);return f; }
    @Test void allFiltersRunTogetherAndSortAtDatabase() {
        CourseFilter f=new CourseFilter();f.setKeyword("Java");f.setCategoryId(webId);f.setActive(true);f.setMinFee(new BigDecimal("2000000"));f.setMaxFee(new BigDecimal("2600000"));
        assertThat(service.search(f)).extracting(Course::getId).containsExactly(springId);
        f.setMinFee(null);f.setMaxFee(null);f.setActive(null);f.setSort("feeDesc");
        assertThat(service.search(f)).extracting(Course::getName).containsExactly("Java Spring","Java Legacy","Java Basic");
        f.setSort("durationAsc");assertThat(service.search(f)).extracting(Course::getDuration).containsExactly(20,30,45);
        f.setSort("feeAsc");assertThat(service.search(f)).extracting(Course::getName).containsExactly("Java Basic","Java Legacy","Java Spring");
    }
    @Test void inactiveRemainsVisibleByDefaultButActiveFilterExcludesIt() {
        CourseFilter f=new CourseFilter();assertThat(service.search(f)).extracting(Course::getId).contains(inactiveId);
        f.setActive(true);assertThat(service.search(f)).extracting(Course::getId).doesNotContain(inactiveId);
        f.setActive(false);assertThat(service.search(f)).extracting(Course::getId).containsExactly(inactiveId);
    }
    @Test void literalWildcardsAreEscaped() {
        service.save(null,form("Java 100%",webId,"400000",10,true));
        CourseFilter f=new CourseFilter();f.setKeyword("%");assertThat(service.search(f)).hasSize(1);
    }
    @Test void duplicateWithinCategoryIsRejectedOnCreateAndEdit() {
        assertThatThrownBy(() -> service.save(null,form("  JAVA SPRING  ",webId,"99",1,true))).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> service.save(inactiveId,form("Java Spring",webId,"99",1,true))).isInstanceOf(BusinessException.class);
        assertThatCode(() -> service.save(springId,form("Java Spring",webId,"2600000",46,true))).doesNotThrowAnyException();
        assertThatCode(() -> service.save(null,form("Java Spring",otherId,"99",1,true))).doesNotThrowAnyException();
    }
    @Test void durationBoundaryIsComputedNotStored() {
        Course c=new Course();c.setDuration(30);assertThat(c.getDurationType()).isEqualTo("Ngắn hạn");c.setDuration(31);assertThat(c.getDurationType()).isEqualTo("Dài hạn");
        Integer count=jdbc.queryForObject("select count(*) from INFORMATION_SCHEMA.COLUMNS where upper(TABLE_NAME)='COURSES' and upper(COLUMN_NAME)='DURATIONTYPE'",Integer.class);
        assertThat(count).isZero();
    }
    @Test void thumbnailSwitchAndAddKeepAtMostOne() {
        service.addImage(springId,image("https://example.com/a.png",true));service.addImage(springId,image("https://example.com/b.png",true));
        assertThat(images.countByCourseIdAndThumbnailTrue(springId)).isEqualTo(1);
        var list=service.detail(springId).getImages();service.setThumbnail(springId,list.get(0).getId());
        assertThat(images.countByCourseIdAndThumbnailTrue(springId)).isEqualTo(1);
        assertThat(service.detail(springId).getThumbnail().getId()).isEqualTo(list.get(0).getId());
        service.deleteImage(springId,list.get(0).getId());assertThat(service.detail(springId).getThumbnail()).isNull();
    }
    @Test void concurrentThumbnailRequestsStillKeepOne() throws Exception {
        service.addImage(springId,image("https://example.com/a.png",false));service.addImage(springId,image("https://example.com/b.png",false));
        var list=service.detail(springId).getImages();ExecutorService pool=Executors.newFixedThreadPool(2);CountDownLatch start=new CountDownLatch(1);
        try {
            Future<?> a=pool.submit(() -> { await(start);service.setThumbnail(springId,list.get(0).getId()); });
            Future<?> b=pool.submit(() -> { await(start);service.setThumbnail(springId,list.get(1).getId()); });
            start.countDown();a.get(15,TimeUnit.SECONDS);b.get(15,TimeUnit.SECONDS);
            assertThat(images.countByCourseIdAndThumbnailTrue(springId)).isEqualTo(1);
        } finally { pool.shutdownNow(); }
    }
    static void await(CountDownLatch latch) { try { latch.await(); } catch(InterruptedException ex) { Thread.currentThread().interrupt();throw new RuntimeException(ex); } }
    @Test void foreignImageCannotBeSelectedAndOldThumbnailSurvives() {
        service.addImage(springId,image("https://example.com/a.png",true));service.addImage(inactiveId,image("https://example.com/b.png",true));
        Long foreignId=service.detail(inactiveId).getThumbnail().getId();
        assertThatThrownBy(() -> service.setThumbnail(springId,foreignId)).isInstanceOf(NotFoundException.class);
        assertThat(images.countByCourseIdAndThumbnailTrue(springId)).isEqualTo(1);
    }
    @Test void invalidImageUrlDoesNotWriteAnything() throws Exception {
        mvc.perform(post("/courses/{id}/images",springId).with(csrf()).param("imageUrl","javascript:alert(1)"))
            .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("imageForm","imageUrl"));
        assertThat(images.count()).isZero();
    }
    @Test void usedCategoryCannotBeDeletedButEmptyCategoryCan() throws Exception {
        mvc.perform(post("/categories/{id}/delete",webId).with(csrf())).andExpect(status().is3xxRedirection()).andExpect(flash().attributeExists("error"));
        assertThat(categories.existsById(webId)).isTrue();
        CourseCategory empty=new CourseCategory();empty.setName("Empty");Long id=categories.saveAndFlush(empty).getId();categoryService.delete(id);assertThat(categories.existsById(id)).isFalse();
    }
    @Test void deletingCourseRemovesRelatedImages() {
        service.addImage(springId,image("https://example.com/a.png",true));service.delete(springId);
        assertThat(courses.existsById(springId)).isFalse();assertThat(images.count()).isZero();
    }
    @Test void missingCourseCategoryAndImageReturnFriendly404() throws Exception {
        mvc.perform(get("/courses/999999")).andExpect(status().isNotFound()).andExpect(view().name("error"));
        mvc.perform(get("/categories/999999/edit")).andExpect(status().isNotFound()).andExpect(view().name("error"));
        mvc.perform(post("/courses/{id}/images/999999/thumbnail",springId).with(csrf())).andExpect(status().isNotFound()).andExpect(view().name("error"));
        mvc.perform(get("/courses/not-a-number")).andExpect(status().isBadRequest()).andExpect(view().name("error"));
    }
    @Test void invalidCreateAndEditPreserveErrorsAndDoNotWrite() throws Exception {
        for(String path:List.of("/courses/create","/courses/"+springId+"/edit")) {
            mvc.perform(post(path).with(csrf()).param("name"," ").param("tuitionFee","0").param("duration","0").param("categoryId",webId.toString()))
                .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("form","name","tuitionFee","duration")).andExpect(view().name("courses/form"));
        }
        assertThat(courses.count()).isEqualTo(4);assertThat(service.detail(springId).getName()).isEqualTo("Java Spring");
    }
    @Test void nonexistentCategoryIsAFormErrorOnCreateAndFilter() throws Exception {
        mvc.perform(post("/courses/create").with(csrf()).param("name","New").param("tuitionFee","100").param("duration","10").param("categoryId","999999"))
            .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("form","categoryId"));
        mvc.perform(get("/courses").param("categoryId","999999")).andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("filter","categoryId"));
    }
    @Test void feeRangeAndNumericBindingErrorsDoNotCrash() throws Exception {
        mvc.perform(get("/courses").param("minFee","200").param("maxFee","100"))
            .andExpect(status().isOk()).andExpect(model().attributeHasErrors("filter")).andExpect(content().string(containsString("200")));
        mvc.perform(get("/courses").param("minFee","abc")).andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("filter","minFee"));
        mvc.perform(post("/courses/create").with(csrf()).param("name","New").param("tuitionFee","abc").param("duration","1").param("categoryId",webId.toString()))
            .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("form","tuitionFee"));
    }
    @Test void allPagesRenderAndEmptyStatesWork() throws Exception {
        for(String path:List.of("/courses","/courses/create","/courses/"+springId,"/courses/"+springId+"/edit","/categories","/categories/create","/categories/"+webId+"/edit"))
            mvc.perform(get(path)).andExpect(status().isOk());
        mvc.perform(get("/courses/{id}",springId)).andExpect(content().string(containsString("Khóa học chưa có ảnh")));
        mvc.perform(get("/courses").param("keyword","xyz-never-exists")).andExpect(content().string(containsString("Không có khóa học để hiển thị")));
        service.addImage(springId,image("https://example.com/a.png",true));service.addImage(springId,image("https://example.com/b.png",false));
        mvc.perform(get("/courses/{id}",springId)).andExpect(status().isOk()).andExpect(content().string(containsString("https://example.com/b.png")));
    }
    @Test void successfulPostCreatesAndEditsAndFilterValuesAreRetained() throws Exception {
        mvc.perform(post("/courses/create").with(csrf()).param("name","New course").param("tuitionFee","500000").param("duration","31").param("active","true").param("categoryId",webId.toString()))
            .andExpect(status().is3xxRedirection()).andExpect(flash().attributeExists("success"));
        mvc.perform(post("/courses/{id}/edit",springId).with(csrf()).param("name","Updated").param("tuitionFee","2700000").param("duration","50").param("active","false").param("categoryId",otherId.toString()))
            .andExpect(status().is3xxRedirection());
        assertThat(service.detail(springId).getName()).isEqualTo("Updated");assertThat(service.detail(springId).isActive()).isFalse();
        var result=mvc.perform(get("/courses").param("keyword","Java").param("categoryId",webId.toString()).param("active","true").param("minFee","100").param("maxFee","3000000").param("sort","feeDesc"))
            .andExpect(status().isOk()).andReturn();
        CourseFilter f=(CourseFilter)result.getModelAndView().getModel().get("filter");
        assertThat(f.getKeyword()).isEqualTo("Java");assertThat(f.getCategoryId()).isEqualTo(webId);assertThat(f.getActive()).isTrue();assertThat(f.getMinFee()).isEqualByComparingTo("100");assertThat(f.getMaxFee()).isEqualByComparingTo("3000000");assertThat(f.getSort()).isEqualTo("feeDesc");
    }
    @Test void postWithoutCsrfTokenIsRejected() throws Exception {
        mvc.perform(post("/courses/{id}/delete",springId)).andExpect(status().isForbidden());assertThat(courses.existsById(springId)).isTrue();
    }
}
