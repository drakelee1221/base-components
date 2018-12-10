/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.feign.configuration.tx.lcn.beans;

import com.alibaba.fastjson.JSONObject;
import com.codingapi.tx.control.service.TransactionControlService;
import com.codingapi.tx.framework.utils.SocketManager;
import com.codingapi.tx.netty.service.MQTxManagerService;
import com.codingapi.tx.netty.service.NettyControlService;
import com.codingapi.tx.netty.service.NettyService;
import com.google.common.util.concurrent.Uninterruptibles;
import com.lorne.core.framework.utils.task.ConditionUtils;
import com.lorne.core.framework.utils.task.IBack;
import com.lorne.core.framework.utils.task.Task;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * NettyControlServiceExtImpl
 *
 * @see com.codingapi.tx.netty.service.impl.NettyControlServiceImpl
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-02-26 10:15
 * @since 4.1.0
 */
@Component
@ConditionalOnProperty(name = "base.rpc.tx.lcn.enable", havingValue = "true")
public class NettyControlServiceExtImpl implements NettyControlService /*, DisposableBean */{

  @Autowired
  private NettyService nettyService;
  @Autowired
  private TransactionControlService transactionControlService;
  @Autowired
  private MQTxManagerService mqTxManagerService;
//  @Autowired
//  private ModelNameService modelNameService;
//  private int threadPoolSize = 100;
//  private ExecutorService threadPool = Executors.newFixedThreadPool(threadPoolSize);

  @Override
  public void restart() {
    nettyService.close();
    Uninterruptibles.sleepUninterruptibly(3, TimeUnit.SECONDS);
    nettyService.start();
  }

  @Override
  public void uploadModelInfo() {
    Thread thread = Executors.defaultThreadFactory().newThread(new Runnable() {
      @Override
      public void run() {
        while (!SocketManager.getInstance().isNetState()) {
          Uninterruptibles.sleepUninterruptibly(5, TimeUnit.SECONDS);
        }
        mqTxManagerService.uploadModelInfo();
      }
    });
    thread.setDaemon(true);
    thread.start();
  }

  @Override
  public void executeService(final ChannelHandlerContext ctx, final String json) {
//    threadPool.execute(new Runnable() {
//      @Override
//      public void run() {
        if (StringUtils.isNotEmpty(json)) {
          JSONObject resObj = JSONObject.parseObject(json);
          if (resObj.containsKey("a")) {
            // tm发送数据给tx模块的处理指令

            transactionControlService.notifyTransactionMsg(ctx, resObj, json);
          } else {
            //tx发送数据给tm的响应返回数据

            String key = resObj.getString("k");
            responseMsg(key, resObj);
          }
        }
//      }
//    });
  }


  private void responseMsg(String key, JSONObject resObj) {
    if (!"h".equals(key)) {
      final String data = resObj.getString("d");
      Task task = ConditionUtils.getInstance().getTask(key);
      if (task != null) {
        if (task.isAwait()) {
          task.setBack(new IBack() {
            @Override
            public Object doing(Object... objs) throws Throwable {
              return data;
            }
          });
          task.signalTask();
        }
      }
    } else {
      //心跳数据
      final String data = resObj.getString("d");
      SocketManager.getInstance().setNetState(true);
      if (StringUtils.isNotEmpty(data)) {
        try {
          SocketManager.getInstance().setDelay(Integer.parseInt(data));
        } catch (Exception e) {
          SocketManager.getInstance().setDelay(1);
        }
      }
    }
  }

//  @Override
//  public void destroy() throws Exception {
//    if (threadPool != null && !threadPool.isTerminated()) {
//      threadPool.shutdown();
//      threadPool = null;
//    }
//  }
}
