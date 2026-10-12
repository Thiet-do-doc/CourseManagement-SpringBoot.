package vn.cmc.coursemanager.controller;
import vn.cmc.coursemanager.exception.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log=LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler(NotFoundException.class)
    public ModelAndView notFound(NotFoundException ex) { return page(HttpStatus.NOT_FOUND, ex.getMessage()); }
    @ExceptionHandler(NoResourceFoundException.class)
    public ModelAndView noResource(NoResourceFoundException ex) { return page(HttpStatus.NOT_FOUND, "Trang bạn yêu cầu không tồn tại."); }
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ModelAndView badId(MethodArgumentTypeMismatchException ex) { return page(HttpStatus.BAD_REQUEST, "ID hoặc tham số không đúng định dạng."); }
    @ExceptionHandler(BusinessException.class)
    public ModelAndView business(BusinessException ex) { return page(HttpStatus.BAD_REQUEST, ex.getMessage()); }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ModelAndView conflict(DataIntegrityViolationException ex) { return page(HttpStatus.CONFLICT, "Dữ liệu đang được sử dụng hoặc bị trùng. Hãy tải lại trang và kiểm tra."); }
    @ExceptionHandler(Exception.class)
    public ModelAndView unexpected(Exception ex) {
        log.error("Unexpected application error", ex);
        return page(HttpStatus.INTERNAL_SERVER_ERROR, "Chưa thể thực hiện yêu cầu. Hãy thử lại hoặc xem nhật ký ứng dụng.");
    }
    private ModelAndView page(HttpStatus status, String message) {
        ModelAndView mv=new ModelAndView("error"); mv.setStatus(status);
        mv.addObject("status",status.value()); mv.addObject("message",message); return mv;
    }
}
