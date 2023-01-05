package com.disaster.locklayer.domain;

import com.disaster.locklayer.infrastructure.annotations.LockLayer;
import com.disaster.locklayer.infrastructure.constant.Constants;
import com.disaster.locklayer.infrastructure.utils.SystemClock;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * The type Lock aspect.
 *
 * @author disaster
 * @version 1.0
 */
@Component
@Aspect
@Slf4j
@ConditionalOnProperty(prefix = Constants.CONFIG_PREFIX, name = "enable", havingValue = "true")
public class LockAspect {

    @Autowired
    private com.disaster.locklayer.infrastructure.persistence.LockLayer lockLayer;

    /**
     * Point cut.
     */
    @Pointcut("@annotation(com.disaster.locklayer.infrastructure.annotations.LockLayer)")
    public void pointCut() {
    }

    /**
     * Do lock object.
     *
     * @param pjp the pjp
     * @return the object
     */
    @Around("pointCut()")
    @SneakyThrows
    public Object doLock(ProceedingJoinPoint pjp) {
        long startTime = SystemClock.now();
        MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
        Method method = methodSignature.getMethod();
        LockLayer lockLayer = method.getAnnotation(LockLayer.class);
        if (lockLayer.reentryLock()) {
            boolean b = this.lockLayer.tryReentryLock(lockLayer.key(), lockLayer.expireTime());
            long endTime = SystemClock.now();
            log.info("lock time = {}", endTime - startTime);
        } else {
            boolean b = this.lockLayer.tryLock(lockLayer.key(), lockLayer.expireTime());
            long endTime = SystemClock.now();
            log.info("lock time = {}", endTime - startTime);
        }
        Object proceed = pjp.proceed();
        this.lockLayer.unLock(lockLayer.key());
        return proceed;
    }

    /**
     * Exception handler.
     *
     * @param jp the jp
     * @param ex the ex
     */
    @AfterThrowing(pointcut = "pointCut()", throwing = "ex")
    public void exceptionHandler(JoinPoint jp, Exception ex) {
        String methodName = jp.getSignature().getName();
        MethodSignature methodSignature = (MethodSignature) jp.getSignature();
        Method method = methodSignature.getMethod();
        LockLayer lockLayer = method.getAnnotation(LockLayer.class);
        log.error("error method {} , exception：{}", methodName, ex.getMessage());
        this.lockLayer.unLock(lockLayer.key());
    }

}
