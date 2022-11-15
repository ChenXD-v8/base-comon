package com.cxd.basecommon.distributedlock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <p>
 * 分布式锁
 * </p>
 *
 * @author xiaodong.chen01@hand-china.com 2021/09/14 17:14
 */
@Component
public class DistributedLockHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(DistributedLockHelper.class);

    private static final String LOCK = "LOCK";
    @Autowired
    private RedisHelper redisHelper;

    /**
     * 加锁
     * 实现方式
     * redis key 过期时间 默认 5s
     *
     * @param module 模块
     * @param keyWords 关键字
     * @return
     */
    public boolean addLock(String module,String keyWords) {
        return addLock(module, keyWords,5);
    }

    /**
     * 加锁
     * @param module 模块
     * @param keyWords 关键字
     * @param second 过期时间 单位 秒
     * @return
     */
    public boolean addLock(String module,String keyWords,long second) {
        String key = generateKey(module,keyWords);
        Boolean isLock = true;
        try {
            synchronized (key){
                redisHelper.setCurrentDatabase(5);
                isLock = redisHelper.strSetIfAbsent(key, Long.toString(System.currentTimeMillis()));
                if(isLock){
                    // 设置过期时间 5s
                    redisHelper.setExpire(key,second);
                    LOGGER.info("[LOCK SUCCESS] add key success. key: {}",key);
                }else {
                    isLock = false;
                    String value = redisHelper.strGet(key);
                    if(value != null){
                        // 判断key值是否超时
                        boolean retryGetLock = (System.currentTimeMillis() - Long.parseLong(value)) > second * 1000;
                        if(retryGetLock){
                            // key超时 重新获取锁 返回加锁成功
                            LOGGER.info("[LOCK ERROR] time out retry get Lock，key: {}",key);
                            redisHelper.strSet(key,Long.toString(System.currentTimeMillis()));
                            redisHelper.setExpire(key,second);
                            isLock = true;
                        }
                    }

                    LOGGER.info("[LOCK ERROR] add lock error, key: {} exist",key);
                }
                redisHelper.clearCurrentDatabase();
            }
        }catch (Exception e){

            LOGGER.error("[LOCK ERROR] get lock error, userName:{}, module:{}, message:{}" , null,module,e.getMessage());
            freedLock(module,keyWords);

        }
        return isLock;
    }
    /**
     * 释放锁
     *
     * @param module
     */
    public void freedLock(String module,String keyWords) {
        String key = generateKey(module,keyWords);
        LOGGER.info("[LOCK FREE] free key: {}",key);
        synchronized (key){
            redisHelper.setCurrentDatabase(5);
            redisHelper.delKey(key);
            redisHelper.clearCurrentDatabase();
        }
    }

    /**
     * 生成redisKey
     * @param module 模块
     * @param keyWords 关键字
     * @return
     */
    private String generateKey(String module,String keyWords) {
        Long userId = -1L;
        return LOCK + ":" + userId + ":" + module+ ":" + keyWords;
    }
}
