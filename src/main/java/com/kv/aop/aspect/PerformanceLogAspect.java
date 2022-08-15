package com.kv.aop.aspect;


import com.kv.util.PerformanceLogContext;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

//@Aspect
@Component
@Log4j2
public class PerformanceLogAspect {
    //Cheat sheet -> https://blog.espenberntsen.net/2010/03/20/aspectj-cheat-sheet/#comments

    @Pointcut("execution(* com.kv.controller.*.*(..))")
    public void performanceLogPointCut() {}

    @Before("performanceLogPointCut()")
    public void beforePerformanceLogAdvice(JoinPoint joinPoint) {
        PerformanceLogContext.setPerformanceLogContext("API_CALL_COUNT", 0);
    }

    @After("performanceLogPointCut()")
    public void afterPerformanceLogAdvice() {
        log.info(PerformanceLogContext.getPerformanceLogContext());
    }
}
