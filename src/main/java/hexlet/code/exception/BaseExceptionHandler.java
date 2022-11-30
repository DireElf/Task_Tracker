package hexlet.code.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;
import java.util.NoSuchElementException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@ResponseBody
@ControllerAdvice
public class BaseExceptionHandler {

    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public final String generalExceptionHandler(Exception exception) {
        return exception.getMessage();
    }

    @ResponseStatus(NOT_FOUND)
    @ExceptionHandler(NoSuchElementException.class)
    public final String noSuchElementExceptionHandler(NoSuchElementException exception) {
        return exception.getMessage();
    }

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler({HttpMessageNotReadableException.class})
    public final String validationExceptionsHandler(Exception exception) {
        return exception.getMessage();
    }

    @ResponseStatus(UNPROCESSABLE_ENTITY)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public final List<ObjectError> validationExceptionsHandler(MethodArgumentNotValidException exception) {
        return exception.getAllErrors();
    }

    @ResponseStatus(UNPROCESSABLE_ENTITY)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public final String validationExceptionsHandler(DataIntegrityViolationException exception) {
        return exception.getCause().getCause().getMessage();
    }

    @ResponseStatus(FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    public final String accessDeniedException(AccessDeniedException exception) {
        return exception.getMessage();
    }

    @ResponseStatus(UNAUTHORIZED)
    @ExceptionHandler(UsernameNotFoundException.class)
    public final String userNotFoundExceptionHandler(UsernameNotFoundException exception) {
        return exception.getMessage();
    }
}
