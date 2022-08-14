package com.kv.aop.aspect;

import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Aspect
@Component
@Log4j2
public class LogExecutionTimeAspect {
    @Around("@annotation(com.kv.aop.annotation.LogExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Object proceed = joinPoint.proceed();
        stopWatch.stop();
        log.info("Method [{}] executed in {} seconds", joinPoint.getSignature(), stopWatch.getTotalTimeSeconds());
        return proceed;
    }

    //Annotation PointCut example
    @Pointcut("execution(@org.springframework.transaction.annotation.Transactional * *(..))")
    public void transactionalMethod() {
    }
}
