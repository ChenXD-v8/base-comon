package com.cxd.basecommon.concurrent.config;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * 自定义线程工厂
 * </p>
 *
 * @author xiaodong.chen01@hand-china.com 2022/07/06 10:19
 */
public class NamedThreadFactory implements ThreadFactory {
    /**
     * 线程名前缀
     */
    private final String prefix;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    public NamedThreadFactory(String prefix){
        this.prefix = prefix;
    }
    @Override
    public Thread newThread(Runnable r) {
        return new Thread(null,r,prefix + "-task-pool-" +threadNumber.getAndIncrement());
    }
}
