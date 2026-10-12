package vn.cmc.coursemanager.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public class CourseForm {
    @NotBlank(message="Tên khóa học không được để trống.") @Size(max=150, message="Tên tối đa 150 ký tự.")
    private String name;
    @NotNull(message="Hãy nhập học phí.") @DecimalMin(value="0", inclusive=false, message="Học phí phải lớn hơn 0.") @Digits(integer=16, fraction=2, message="Học phí tối đa 16 chữ số nguyên và 2 chữ số thập phân.")
    private BigDecimal tuitionFee;
    @NotNull(message="Hãy nhập thời lượng.") @Min(value=1, message="Thời lượng phải lớn hơn 0.")
    private Integer duration;
    
    private boolean active = true;
    @Size(max=4000, message="Mô tả tối đa 4000 ký tự.")
    private String description;
    @NotNull(message="Hãy chọn loại khóa học.")
    private Long categoryId;
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
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
}
