package vn.cmc.coursemanager.exception;
public class BusinessException extends RuntimeException {
    private final String field;
    public BusinessException(String field, String message) { super(message); this.field = field; }
    public String getField() { return field; }
}
