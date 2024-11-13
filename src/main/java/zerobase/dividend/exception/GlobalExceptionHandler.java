package zerobase.dividend.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ErrorResponse handleGeneralException(Exception e) {
        log.error("Global Exception 예외 발생: {}", e.getMessage());
        return new ErrorResponse(INTERNAL_SERVER_ERROR.value(), e.getMessage());
    }
}
