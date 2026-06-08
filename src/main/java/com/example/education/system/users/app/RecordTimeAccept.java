package com.example.education.system.users.app;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class RecordTimeAccept {

    @Pointcut("execution(* com.example.education.system.users..*.*(..))")
    public void appPackage() {
    }

    @Around("appPackage()")
    public Object recordTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long cost = System.currentTimeMillis() - start;

        String method = joinPoint.getSignature().toShortString();
        log.info("{} 执行耗时: {}ms", method, cost);
        return result;
    }
}
