package com.example.pfe.auth.exception;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public Map<String,String> handleRuntime(RuntimeException ex){
        Map<String,String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return error;
    }
}
