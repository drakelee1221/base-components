/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.common.service;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池服务
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2017-07-14 14:19
 */
@Service
@RefreshScope
public class ThreadPoolService {

  @Value("${base.common.threadPool.poolSize:100}")
  private int threadPoolSize;
  @Value("${base.common.threadPool.keepAliveTime:10000}")
  private long keepAliveTime;

  private ExecutorService executorService;

  /**
   * 异步调用，无返回值
   * @param runnable -
   */
  public void run(Runnable runnable){
    executorService.execute(runnable);
  }

  /**
   * 异步调用，返Future
   * @param callable -
   */
  public <T> Future<T> call(Callable<T> callable){
    return executorService.submit(callable);
  }


  /**
   * 主线程等待，多线程异步调用
   * @param callableList 调用接口集合，不能为空，每个单独使用一个线程调用
   * @return List
   */
  public <T> List<T> callWithMainThreadWait(List<Callable<T>> callableList){
    Assert.notEmpty(callableList, "调用接口集合不能为空");
    CountDownLatch countDownLatch = new CountDownLatch(callableList.size());
    List<Future<T>> futureList = Lists.newArrayList();
    List<T> results = Lists.newArrayList();
    for (Callable<T> callable : callableList) {
      futureList.add(
        executorService.submit(new Callable<T>() {
          @Override
          public T call() throws Exception {
            try{
              return callable.call();
            }finally {
              countDownLatch.countDown();
            }
          }
        })
      );
    }
    try {
      countDownLatch.await();
      for (Future<T> f : futureList) {
        results.add(f.get());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return results;
  }

  /**
   * 主线程等待，多线程异步调用
   * @param threadCount 线程数，需大于零
   * @param callable 调用逻辑代码
   * @return List
   */
  public <T> List<T> callWithMainThreadWait(int threadCount, Callable<T> callable){
    Assert.isTrue(threadCount > 0, "线程数需大于0");
    CountDownLatch countDownLatch = new CountDownLatch(threadCount);
    List<Future<T>> futureList = Lists.newArrayList();
    List<T> results = Lists.newArrayList();
    for (int i = 0; i < threadCount; i++) {
      futureList.add(
        executorService.submit(new Callable<T>() {
          @Override
          public T call() throws Exception {
            try{
              return callable.call();
            }finally {
              countDownLatch.countDown();
            }
          }
        })
      );
    }
    try {
      countDownLatch.await();
      for (Future<T> f : futureList) {
        results.add(f.get());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return results;
  }

  /**
   * 主线程等待，多线程异步调用
   * @param runnableList 调用接口集合，不能为空，每个单独使用一个线程调用
   */
  public void runWithMainThreadWait(List<Runnable> runnableList){
    Assert.notEmpty(runnableList, "调用接口集合不能为空");
    CountDownLatch countDownLatch = new CountDownLatch(runnableList.size());
    for (Runnable runnable : runnableList) {
      executorService.execute(new Runnable() {
        @Override
        public void run() {
          try{
            runnable.run();
          }finally {
            countDownLatch.countDown();
          }
        }
      });
    }
    try {
      countDownLatch.await();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  /**
   * 主线程等待，多线程异步调用
   * @param threadCount 线程数，需大于
   * @param runnable 调用逻辑代码
   */
  public void runWithMainThreadWait(int threadCount, Runnable runnable){
    Assert.isTrue(threadCount > 0, "线程数需大于0");
    CountDownLatch countDownLatch = new CountDownLatch(threadCount);
    for (int i = 0; i < threadCount; i++) {
      executorService.execute(new Runnable() {
        @Override
        public void run() {
          try{
            runnable.run();
          }finally {
            countDownLatch.countDown();
          }
        }
      });
    }
    try {
      countDownLatch.await();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }


  /**
   * 得到执行接口
   * @return Executor
   */
  public Executor getExecutor(){
    return executorService;
  }

  @PostConstruct
  @SuppressWarnings("all")
  public void init(){
    if(keepAliveTime < 0){
      keepAliveTime = 0;
    }
    ThreadPoolExecutor e = new ThreadPoolExecutor(threadPoolSize, threadPoolSize, keepAliveTime, TimeUnit.MILLISECONDS,
                                                  new LinkedBlockingQueue<Runnable>(), Executors.defaultThreadFactory()
    );
    if(keepAliveTime > 0){
      e.allowCoreThreadTimeOut(true);
    }
    executorService = e;
  }

  @PreDestroy
  public void destroy() {
    if (executorService != null && !executorService.isTerminated()) {
      executorService.shutdown();
      executorService = null;
    }
  }

  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    destroy();
  }


  /**
   * 用于main方法测试用，使用完需调用 destroy 方法；
   */
  @Deprecated
  public static ThreadPoolService testBuild(int threadPoolSize){
    ThreadPoolService threadPoolService = new ThreadPoolService();
    threadPoolService.threadPoolSize = threadPoolSize;
    threadPoolService.init();
    return threadPoolService;
  }

}
