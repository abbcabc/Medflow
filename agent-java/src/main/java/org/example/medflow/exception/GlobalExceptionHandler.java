package org.example.medflow.exception;

import lombok.extern.slf4j.Slf4j;
import org.example.medflow.vo.R;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice // 全局异常处理，返回JSON
public class GlobalExceptionHandler {

    // 捕获所有运行时异常
    @ExceptionHandler(RuntimeException.class)
    public R<Void> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常：", e);
        return R.error(e.getMessage());
    }

    // 捕获所有异常
    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e) {
        log.error("系统异常：", e);
        return R.error("系统内部异常，请联系管理员");
    }
}