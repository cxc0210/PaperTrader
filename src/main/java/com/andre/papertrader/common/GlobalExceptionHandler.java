package com.andre.papertrader.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusiness(BusinessException e) {
        log.warn("业务请求失败，code={}, message={}", e.getCode(), e.getMessage());
        return ResponseEntity.ok(Result.error(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Result<Void>> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.warn("数据完整性校验失败", e);
        return ResponseEntity.ok(Result.error(ErrorCode.DATA_CONFLICT, "数据冲突，请检查用户名、邮箱或手机号是否已存在"));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Result<Void>> handleResponseStatus(ResponseStatusException e) {
        log.warn("请求被拒绝，status={}, message={}", e.getStatusCode().value(), e.getReason());
        return ResponseEntity.ok(Result.error(e.getStatusCode().value(), e.getReason()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleOther(Exception e) {
        log.error("未处理的系统异常", e);
        return ResponseEntity.ok(Result.error(500, "服务器内部错误"));
    }
}
