package com.cxd.basecommon.distributedlock;

import java.lang.reflect.Method;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * <p>
 *  分布式锁切面
 * </p>
 *
 * @author xiaodong.chen01@hand-china.com 2021/10/26 15:44
 */
@Component
@Aspect
@Slf4j
public class AddLockAspect {

    @Autowired
    private DistributedLockHelper distributedLockHelper;



    @Around("@annotation(DistributedLock)")
    public Object addLock(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        log.info("==============[distribute lock aop]==========================");
        MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
        Method method = signature.getMethod();
        DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);
        if (distributedLock != null){
            // 拿到关键字段
            String keyWord = StringUtils.isEmpty(distributedLock.keyWords()) ?
                    getKeyWordFromArgs(proceedingJoinPoint.getArgs(), distributedLock.argIndex(), distributedLock.fieldName())
                    : distributedLock.keyWords();
            // 加锁
            boolean lockSuccess = distributedLockHelper.addLock(distributedLock.module(), keyWord);
            if(lockSuccess){
                log.info("[LOCK SUCCESS] start execute business module: {}",method.getName());
                Object proceed = proceedingJoinPoint.proceed();
                distributedLockHelper.freedLock(distributedLock.module(),keyWord);
                return proceed;
            }else {
                log.error("[LOCK ERROR] do not execute business execute method");
                return buildReturnData(method.getReturnType());
            }
        }else {
            return proceedingJoinPoint.proceed();

        }
    }


    /**
     * 从方法参数中获取关键值
     * @param args 参数
     * @param index 索引
     * @param fieldName 字段名
     * @return
     */
    private String getKeyWordFromArgs(Object[] args,int index,String fieldName){
        String keyWord = "";
        try {
            if(index == -1){
                return keyWord;
            }
            if(index < args.length){
                Object arg = args[index];
                if(arg instanceof String){
                    keyWord = arg.toString();
                } else {
                    Object value = ReflectionUtils.getFieldValue(arg, fieldName);
                    if (value != null){
                        keyWord = String.valueOf(value);
                    }
                }
            }
        }catch (Exception e){
            log.error("[LOCK ERROR] get key word from args error: {}",e.getMessage());
        }
        return keyWord;
    }

    /**
     * 构建返回值
     * @param returnType class
     * @return
     */
    private Object buildReturnData(Class<?> returnType){
        try {
            if (ResponseEntity.class == returnType){
                return ResponseEntity.ok();
            }
            if (!"void".equals(returnType.toString()) && !returnType.isPrimitive()){
                return returnType.newInstance();
            }
            return null;
        }catch (Exception e){
            log.error("[LOCK ERROR] build return data error ,{}",e.getMessage());
            return null;
        }

    }
}
