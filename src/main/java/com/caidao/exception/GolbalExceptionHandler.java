package com.caidao.exception;

import org.mybatis.logging.Logger;
import org.mybatis.logging.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GolbalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GolbalExceptionHandler.class);

    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public Object defaultErrorHandler(HttpServletRequest request,Exception e){
        return null;
    }


}
