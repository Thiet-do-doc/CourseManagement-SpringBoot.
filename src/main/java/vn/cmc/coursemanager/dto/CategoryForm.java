package vn.cmc.coursemanager.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public class CategoryForm {
    @NotBlank(message="Tên loại không được để trống.") @Size(max=120, message="Tên tối đa 120 ký tự.")
    private String name;
    @Size(max=2000, message="Mô tả tối đa 2000 ký tự.")
    private String description;
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
