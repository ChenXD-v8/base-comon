package com.cxd.basecommon.concurrent.service;

import java.util.ArrayList;
import java.util.List;

import com.cxd.basecommon.concurrent.dto.ConcurrentReturnData;
import com.cxd.basecommon.concurrent.util.ThreadPoolToolUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 并发批量执行通用类
 * </p>
 *
 * @author xiaodong.chen01@hand-china.com 2022/09/27 15:31
 */
@Slf4j
public abstract class ConcurrentExecuteAbstractService<K,V> {

    /**
     * 遍历数据
     * @param tenantId 租户
     * @param dataList 数据
     * @param module 模块
     * @return ConcurrentReturnData
     */
    public abstract ConcurrentReturnData<V> batchProcessor(Long tenantId, List<K> dataList, String module);

    /**
     * 初始化全局参数
     */
    public abstract void initialization();

    /**
     * 清除全局参数
     */
    public abstract void clearParameter();

    public ConcurrentReturnData<V> execute(Long tenantId, List<K> dataList) {
        return execute(tenantId, dataList, "批量执行");
    }

    public ConcurrentReturnData<V> execute(Long tenantId, List<K> dataList, String module) {
        return execute(tenantId, dataList, module, 5);
    }

    /**
     * 并发 调用多线程分批执行数据
     * @param tenantId 租户
     * @param dataList 数据list
     * @param module 执行模块
     * @param maxRunSize 最大线程数
     * @return 执行结果
     */
    public ConcurrentReturnData<V> execute(Long tenantId, List<K> dataList, String module, int maxRunSize) {
        log.info("【{}】 开始执行====================", module);
        // 初始化全局参数
        initialization();
        int size = dataList.size();
        if (size < 100) {
            // 执行大小小于100原方法执行
            return this.batchProcessor(tenantId, dataList, module);
        }

        int runSize = size / 100 + 1;
        runSize = Math.min(runSize, maxRunSize);

        ThreadPoolToolUtil<K,V> threadPoolTool = new ThreadPoolToolUtil<>(dataList, runSize, this, tenantId, module);
        ConcurrentReturnData<V> returnData = threadPoolTool.execute();
        log.info("【{}】 执行结束====================", module);
        // 清除全局参数
        clearParameter();
        return returnData;
    }
    public ConcurrentReturnData<V> initReturnData(){
        return new ConcurrentReturnData<>(0, 0, 0, new ArrayList<>(), new ArrayList<>());
    }
}
