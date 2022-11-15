package com.cxd.basecommon.distributedlock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * 分布式锁注解
 * </p>
 *
 * @author xiaodong.chen01@hand-china.com 2022/08/01 16:29
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {
    /**
     * 模块
     * @return
     */
    String module();

    /**
     * 关键字
     * @return
     */
    String keyWords() default "";
    int argIndex() default -1;
    String fieldName() default "";
}
