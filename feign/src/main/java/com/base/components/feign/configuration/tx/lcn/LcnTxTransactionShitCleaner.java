/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.feign.configuration.tx.lcn;

import com.codingapi.tx.framework.utils.SocketManager;
import com.codingapi.tx.netty.service.NettyService;
import com.google.common.util.concurrent.Uninterruptibles;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.EventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * LcnTxTransactionShitCleaner  优雅停机时，帮LCN擦狗屎
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-01-26 14:25
 * @since 4.1.0
 */
@SuppressWarnings("all")
public class LcnTxTransactionShitCleaner {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /**
   * 擦狗屎
   */
  public static void shitCleaner(NettyService nettyService){
    try {
//      cleanSocketManager();
      cleanNettyServiceImpl(nettyService);
//      cleanNettyControlServiceImpl(nettyControlService);
    } catch (Exception e) {
      logger.error("shit clean error > lcn", e);
    }
    logger.debug("shit is clean up > lcn");
  }

  private static Field getField(Object instance, String fieldName){
    if(instance != null){
      Class<?> c = instance.getClass();
      try {
        Field field = c.getDeclaredField(fieldName);
        if(field != null){
          field.setAccessible(true);
          return field;
        }
      } catch (Exception e) {
        logger.error(c + " cannot such field > " + fieldName, e);
      }
    }
    return null;
  }

  private static <T> T getValue(Object instance, Field field){
    if(instance != null && field != null){
      try {
        return (T) field.get(instance);
      } catch (Exception e) {
        logger.error("", e);
      }
    }
    return null;
  }

  private static void setValue(Object instance, Field field, Object value){
    if(instance != null && field != null){
      try {
        field.set(instance, value);
      } catch (Exception e) {
        logger.error("", e);
      }
    }
  }

  private static void cleanSocketManager() {
    SocketManager socketManager = SocketManager.getInstance();
    if(socketManager != null) {
      Field ctxField = getField(socketManager, "ctx");
      ChannelHandlerContext ctx = getValue(socketManager, ctxField);
      if (ctx != null) {
        ctx.flush();
        setValue(socketManager, ctxField, null);
        if (ctx.channel() != null) {
          ctx.channel().eventLoop().shutdownGracefully();
          ctx.channel().close();
        }
        ctx.close();
        socketManager.setNetState(true);
        logger.debug("shit cleaner > SocketManager > [ChannelHandlerContext ctx]");
      }


      Field threadPoolField = getField(socketManager, "threadPool");
      ExecutorService threadPool = getValue(socketManager, threadPoolField);
      if (threadPool != null) {
        threadPool.shutdown();
        logger.debug("shit cleaner > SocketManager > [Executor threadPool]");
      }

      Field executorServiceField = getField(socketManager, "executorService");
      ExecutorService executorService = getValue(socketManager, executorServiceField);
      if (executorService != null) {
        executorService.shutdown();
        logger.debug("shit cleaner > SocketManager > [ScheduledExecutorService executorService]");
      }
    }
  }


  private static void cleanNettyServiceImpl(NettyService nettyService){
    Field workerGroupField = getField(nettyService, "workerGroup");
    EventLoopGroup workerGroup = getValue(nettyService, workerGroupField);
    while (workerGroup != null && !workerGroup.isShutdown()){
      Field isStartingField = getField(nettyService,"isStarting");
      setValue(nettyService, isStartingField, true);
      logger.debug("shit cleaner > NettyServiceImpl [boolean isStarting = true]");
      if(workerGroup != null){
        for (EventExecutor eventExecutor : workerGroup) {
          eventExecutor.shutdownGracefully();
          eventExecutor.shutdown();
          eventExecutor.shutdownNow();
        }
        workerGroup.shutdownGracefully();
        workerGroup.shutdown();
        workerGroup.shutdownNow();
        setValue(nettyService, workerGroupField, null);
        logger.debug("shit cleaner > NettyServiceImpl [EventLoopGroup workerGroup]");
      }
      Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
    }
  }

//  private static void cleanNettyControlServiceImpl(NettyControlServiceImpl nettyControlService){
//    Field threadPoolField = getField(nettyControlService, "threadPool");
//    ExecutorService threadPool = getValue(nettyControlService, threadPoolField);
//    if(threadPool != null){
//      threadPool.shutdown();
//      logger.debug("shit cleaner > NettyControlServiceImpl [Executor threadPool]");
//    }
//  }
}
