package Accessment.LibraryManagment_BE.exception;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice

public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity handle(BusinessException ex){

        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
