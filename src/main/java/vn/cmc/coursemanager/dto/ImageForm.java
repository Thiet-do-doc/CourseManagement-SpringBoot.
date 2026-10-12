package vn.cmc.coursemanager.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public class ImageForm {
    @NotBlank(message="Hãy nhập URL ảnh.") @Size(max=2000, message="URL tối đa 2000 ký tự.")
    private String imageUrl;
    
    private boolean thumbnail;
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public boolean isThumbnail() { return thumbnail; }
    public void setThumbnail(boolean thumbnail) { this.thumbnail = thumbnail; }
}
