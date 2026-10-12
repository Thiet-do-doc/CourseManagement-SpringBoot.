package vn.cmc.coursemanager.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public class CourseFilter {
    @Size(max=150, message="Từ khóa tối đa 150 ký tự.")
    private String keyword;
    
    private Long categoryId;
    
    private Boolean active;
    @DecimalMin(value="0", message="Học phí tối thiểu không được âm.")
    private BigDecimal minFee;
    @DecimalMin(value="0", message="Học phí tối đa không được âm.")
    private BigDecimal maxFee;
    @Pattern(regexp="feeAsc|feeDesc|durationAsc", message="Kiểu sắp xếp không hợp lệ.")
    private String sort = "feeAsc";
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public BigDecimal getMinFee() { return minFee; }
    public void setMinFee(BigDecimal minFee) { this.minFee = minFee; }
    public BigDecimal getMaxFee() { return maxFee; }
    public void setMaxFee(BigDecimal maxFee) { this.maxFee = maxFee; }
    public String getSort() { return sort; }
    public void setSort(String sort) { this.sort = sort; }
    @AssertTrue(message="Học phí tối thiểu không được lớn hơn học phí tối đa.")
    public boolean isFeeRangeValid() { return minFee == null || maxFee == null || minFee.compareTo(maxFee) <= 0; }
}
