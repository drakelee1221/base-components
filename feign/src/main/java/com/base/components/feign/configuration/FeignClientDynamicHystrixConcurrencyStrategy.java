/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.feign.configuration;

import com.base.components.feign.header.DynamicHeaderRegistry;
import com.google.common.collect.Maps;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import com.netflix.hystrix.strategy.HystrixPlugins;
import com.netflix.hystrix.strategy.concurrency.HystrixConcurrencyStrategy;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestVariable;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestVariableLifecycle;
import com.netflix.hystrix.strategy.eventnotifier.HystrixEventNotifier;
import com.netflix.hystrix.strategy.executionhook.HystrixCommandExecutionHook;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisher;
import com.netflix.hystrix.strategy.properties.HystrixPropertiesStrategy;
import com.netflix.hystrix.strategy.properties.HystrixProperty;
import com.base.components.common.util.StackTraceHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 动态传递FeignClient的Header的Hystrix断路器策略
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2017-11-30 15:35
 */
public class FeignClientDynamicHystrixConcurrencyStrategy extends HystrixConcurrencyStrategy{
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final String CURRENT_PACKAGE = FeignClientDynamicHystrixConcurrencyStrategy.class.getPackage().getName();
  private String customPackagePrefix;
  static final ThreadLocal<Map<String, String>> FEIGN_CLIENT_HEADERS_THREAD_LOCAL =
    ThreadLocal.withInitial(Maps::newHashMap);

  public static final ThreadLocal<String> FEIGN_CLIENT_STACK_THREAD_LOCAL = new ThreadLocal<>();

  private HystrixConcurrencyStrategy delegate;

  public FeignClientDynamicHystrixConcurrencyStrategy(String customPackagePrefix) {
    try {
      this.customPackagePrefix = customPackagePrefix;
      this.delegate = HystrixPlugins.getInstance().getConcurrencyStrategy();
      if (this.delegate instanceof FeignClientDynamicHystrixConcurrencyStrategy) {
        // Welcome to singleton hell...
        return;
      }
      HystrixCommandExecutionHook commandExecutionHook = HystrixPlugins
        .getInstance().getCommandExecutionHook();
      HystrixEventNotifier eventNotifier = HystrixPlugins.getInstance()
                                                         .getEventNotifier();
      HystrixMetricsPublisher metricsPublisher = HystrixPlugins.getInstance()
                                                               .getMetricsPublisher();
      HystrixPropertiesStrategy propertiesStrategy = HystrixPlugins.getInstance()
                                                                   .getPropertiesStrategy();
      logCurrentStateOfHystrixPlugins(eventNotifier, metricsPublisher,
                                      propertiesStrategy);
      HystrixPlugins.reset();
      HystrixPlugins.getInstance().registerConcurrencyStrategy(this);
      HystrixPlugins.getInstance()
                    .registerCommandExecutionHook(commandExecutionHook);
      HystrixPlugins.getInstance().registerEventNotifier(eventNotifier);
      HystrixPlugins.getInstance().registerMetricsPublisher(metricsPublisher);
      HystrixPlugins.getInstance().registerPropertiesStrategy(propertiesStrategy);
    }
    catch (Exception e) {
      logger.error("Failed to register Dynamic Header Concurrency Strategy", e);
    }
  }

  @Override
  public ThreadPoolExecutor getThreadPool(HystrixThreadPoolKey threadPoolKey, HystrixProperty<Integer> corePoolSize,
                                          HystrixProperty<Integer> maximumPoolSize,
                                          HystrixProperty<Integer> keepAliveTime, TimeUnit unit,
                                          BlockingQueue<Runnable> workQueue) {
    return this.delegate.getThreadPool(threadPoolKey, corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
  }

  @Override
  public ThreadPoolExecutor getThreadPool(HystrixThreadPoolKey threadPoolKey,
                                          HystrixThreadPoolProperties threadPoolProperties) {
    return this.delegate.getThreadPool(threadPoolKey, threadPoolProperties);
  }

  @Override
  public BlockingQueue<Runnable> getBlockingQueue(int maxQueueSize) {
    return this.delegate.getBlockingQueue(maxQueueSize);
  }

  @Override
  public <T> HystrixRequestVariable<T> getRequestVariable(HystrixRequestVariableLifecycle<T> rv) {
    return this.delegate.getRequestVariable(rv);
  }

  @Override
  public <T> Callable<T> wrapCallable(Callable<T> callable) {
    if (callable instanceof DynamicHeaderCallable) {
      return callable;
    }
    Callable<T> wrappedCallable = this.delegate != null
                                  ? this.delegate.wrapCallable(callable) : callable;
    if (wrappedCallable instanceof DynamicHeaderCallable) {
      return wrappedCallable;
    }
    return new DynamicHeaderCallable<>(wrappedCallable);
  }

  class DynamicHeaderCallable<S> implements Callable<S> {

    private Callable<S> callable;

    private Map<String, String> headers;

    private String stack;

    /** 主线程 */
    DynamicHeaderCallable(Callable<S> callable) {
      this.stack = StackTraceHelper.getTargetStack(customPackagePrefix,
                                                   CURRENT_PACKAGE);
      this.callable = callable;
      this.headers = DynamicHeaderRegistry.getAllHeaders(stack);
    }

    /** 子线程 */
    @Override
    public S call() throws Exception {
      S call;
      try {
        FEIGN_CLIENT_HEADERS_THREAD_LOCAL.get().putAll(headers);
        FEIGN_CLIENT_STACK_THREAD_LOCAL.set(stack);
        call = callable.call();
      }finally {
        FEIGN_CLIENT_HEADERS_THREAD_LOCAL.get().clear();
        FEIGN_CLIENT_STACK_THREAD_LOCAL.remove();
      }
      return call;
    }
  }


  private void logCurrentStateOfHystrixPlugins(HystrixEventNotifier eventNotifier,
                                               HystrixMetricsPublisher metricsPublisher,
                                               HystrixPropertiesStrategy propertiesStrategy) {
    if (logger.isDebugEnabled()) {
      logger.debug("Current Hystrix plugins configuration is [" + "concurrencyStrategy ["
                     + this.delegate + "]," + "eventNotifier [" + eventNotifier + "],"
                     + "metricPublisher [" + metricsPublisher + "]," + "propertiesStrategy ["
                     + propertiesStrategy + "]," + "]");
      logger.debug("Registering Sleuth Hystrix Concurrency Strategy.");
    }
  }
}
