package com.cxd.basecommon.concurrent.util;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.cxd.basecommon.concurrent.config.NamedThreadFactory;
import com.cxd.basecommon.concurrent.dto.ConcurrentReturnData;
import com.cxd.basecommon.concurrent.service.ConcurrentExecuteAbstractService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;


/**
 * <p>
 * 线程池处理
 * </p>
 *
 * @author xiaodong.chen01@hand-china.com 2021/08/11 14:42
 */
@Slf4j
public class ThreadPoolToolUtil<K,V> {

    private final List<K> list;

    private ConcurrentReturnData<V> returnData;
    /**
     * 计数器
     */
    private final CountDownLatch countDownLatch;

    /**
     * 单个线程处理的数据量
     */
    private int singleCount;
    /**
     * 处理的总数据量
     */
    private int listSize;
    /**
     * 开启的线程数
     */
    private final int runSize;
    private final Long tenantId;
    private final String module;

    private final ConcurrentExecuteAbstractService<K,V> concurrentExecuteService;

    public ThreadPoolToolUtil(List<K> list, int runSize, ConcurrentExecuteAbstractService<K,V> executeService, Long tenantId, String module) {
        this.runSize = runSize;
        this.list = list;
        this.concurrentExecuteService = executeService;
        this.tenantId = tenantId;
        this.module = module;
        if (list != null) {
            this.listSize = list.size();
            this.singleCount = (this.listSize / this.runSize);
        }
        this.countDownLatch = new CountDownLatch(runSize);
    }

    public ConcurrentReturnData<V> execute() {
        ThreadPoolExecutor executor = null;
        try {
            //阻塞队列大小
            //拒绝策略，原线程执行
            executor = new ThreadPoolExecutor(this.runSize, this.runSize + 2,
                    10,
                    TimeUnit.SECONDS,
                    //阻塞队列大小
                    new ArrayBlockingQueue<>(50),
                    new NamedThreadFactory(module),
                    //拒绝策略，原线程执行
                    new ThreadPoolExecutor.CallerRunsPolicy());

            returnData = concurrentExecuteService.initReturnData();
            //创建线程
            int startIndex,endIndex;
            List<K> newList;
            for (int i = 0; i < runSize; i++) {
                //计算每个线程对应的数据
                if (i < (runSize - 1)) {
                    startIndex = i * singleCount;
                    endIndex = (i + 1) * singleCount;
                    newList = list.subList(startIndex, endIndex);
                } else {
                    startIndex = i * singleCount;
                    endIndex = listSize;
                    newList = list.subList(startIndex, endIndex);
                }
                //执行线程
                executor.execute(new MyTask(newList, countDownLatch,i));
            }
            countDownLatch.await();
        }catch (Exception e){
            e.printStackTrace();
            log.error("【{}】 threadPool execute error: {}",module ,e.getMessage());
        }finally {
            if (executor != null){
                executor.shutdown();
            }
        }

        return this.returnData;

    }

    private class MyTask implements Runnable {
        List<K> list;
        CountDownLatch countDownLatch;
        int index;

        public MyTask(List<K> list, CountDownLatch countDownLatch,int index) {
            this.list = list;
            this.countDownLatch = countDownLatch;
            this.index = index;
        }

        @Override
        public void run() {
            StopWatch stopWatch = new StopWatch(module+"-task-"+index);
            try {
                stopWatch.start("task-" +index);
                ConcurrentReturnData<V> returnData1 = concurrentExecuteService.batchProcessor(tenantId, this.list, module);
                returnData.add(returnData1);
            } catch (Exception e) {
                log.error(e.getMessage());
            } finally {
                countDownLatch.countDown();
                stopWatch.stop();
                log.info( "【{}】 {} 耗时： {}ms",module,stopWatch.getLastTaskName(),stopWatch.getLastTaskTimeMillis());
            }
        }
    }

}
