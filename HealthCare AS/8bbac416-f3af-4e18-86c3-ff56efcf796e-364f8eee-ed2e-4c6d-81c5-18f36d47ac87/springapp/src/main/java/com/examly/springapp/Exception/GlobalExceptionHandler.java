package com.examly.springapp.Exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,String>>handleValidationException(MethodArgumentNotValidException e){
        Map<String,String>response=new HashMap<>();
        String errorMessage=e.getBindingResult().getFieldError().getDefaultMessage();
        response.put("message", errorMessage);
        return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
    }
}
