package com.markerhub.common.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.markerhub.common.lang.Result;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 统一缓存处理
 * @author mingchiuli
 * @create 2021-12-01 7:48 AM
 */
@Aspect
@Component
@Slf4j
public class CacheAspect {

    private static final String LOCK = "lock:";

    RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private void setRedisTemplateImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    ObjectMapper objectMapper;

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Pointcut("@annotation(com.markerhub.common.cache.Cache)")
    public void pt() {}

    @SneakyThrows
    @Around("pt()")
    public Object around(ProceedingJoinPoint pjp) {
        Signature signature = pjp.getSignature();
        //类名
        String className = pjp.getTarget().getClass().getSimpleName();
        //调用的方法名
        String methodName = signature.getName();

        Class<?>[] parameterTypes = new Class[pjp.getArgs().length];
        Object[] args = pjp.getArgs();
        //参数
        StringBuilder params = new StringBuilder();

        for (int i = 0; i < args.length; i++) {
            if (args[i] != null) {
                //方法的参数必须是能够json化的
                params.append(objectMapper.writeValueAsString(args[i]));
                params.append("::");
//                params.append(args[i]);
                parameterTypes[i] = args[i].getClass();
            } else {
                parameterTypes[i] = null;
            }
        }
//        if (StringUtils.hasLength(params.toString())) {
//            params = new StringBuilder(Arrays.toString(DigestUtil.md5(params.toString())));
//            params = new StringBuilder(params.toString());
//        }
        Method method = pjp.getSignature().getDeclaringType().getMethod(methodName, parameterTypes);

//        MethodSignature methodSignature = (MethodSignature) signature;
//        Method method = methodSignature.getMethod();

        Cache annotation = method.getAnnotation(Cache.class);
        long expire = annotation.expire();
        String name = annotation.name();

        String redisKey = name + "::" + className + "::" + methodName + "::" + params;
//        String redisKey = name + "::" + params;

        Object o = redisTemplate.opsForValue().get(redisKey);
        if (o != null) {
            return objectMapper.convertValue(o, Result.class);
        }

        String lock = (LOCK +  methodName + params).intern();

        //防止缓存击穿
        synchronized (lock) {
            log.info("线程{}拿到锁{}", Thread.currentThread().getName(), lock);
            //双重检查
            Object r = redisTemplate.opsForValue().get(redisKey);

            if (r != null) {
                log.info("线程{}释放锁{}", Thread.currentThread().getName(), lock);
                return objectMapper.convertValue(r, Result.class);
            }
            //执行目标方法
            Object proceed = pjp.proceed();
            redisTemplate.opsForValue().set(redisKey, proceed, expire, TimeUnit.MINUTES);
            log.info("线程{}查库并释放锁{}", Thread.currentThread().getName(), lock);
            return proceed;
        }

    }
}
