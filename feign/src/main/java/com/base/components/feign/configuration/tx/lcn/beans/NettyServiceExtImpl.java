/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.feign.configuration.tx.lcn.beans;

import com.codingapi.tx.Constants;
import com.codingapi.tx.framework.utils.SocketManager;
import com.codingapi.tx.netty.handler.TransactionHandler;
import com.codingapi.tx.netty.service.NettyControlService;
import com.codingapi.tx.netty.service.NettyDistributeService;
import com.codingapi.tx.netty.service.NettyService;
import com.google.common.util.concurrent.Uninterruptibles;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.EventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * NettyServiceExtImpl
 *
 * @see com.codingapi.tx.netty.service.impl.NettyServiceImpl
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-06-01 11:16
 * @since 4.1.0
 */
@Service
@ConditionalOnProperty(name = "base.rpc.tx.lcn.enable", havingValue = "true")
public class NettyServiceExtImpl implements NettyService, DisposableBean {
  @Autowired
  private NettyDistributeService nettyDistributeService;
  @Autowired
  private NettyControlService nettyControlService;
  private EventLoopGroup workerGroup;
  private static volatile boolean isStarting = false;

  private Logger logger = LoggerFactory.getLogger(NettyServiceExtImpl.class);

  private ExecutorService threadPool = Executors.newFixedThreadPool(100);

  @Override
  public synchronized void start() {
    if (isStarting) {
      return;
    }
    isStarting = true;
    nettyDistributeService.loadTxServer();

    String host = Constants.txServer.getHost();
    int port = Constants.txServer.getPort();
    final int heart = Constants.txServer.getHeart();
    int delay = Constants.txServer.getDelay();

    final TransactionHandler transactionHandler = new TransactionHandler(threadPool, nettyControlService, delay);
    workerGroup = new NioEventLoopGroup();
    try {
      Bootstrap b = new Bootstrap(); // (1)
      b.group(workerGroup); // (2)
      b.channel(NioSocketChannel.class); // (3)
      b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
      b.handler(new ChannelInitializer<SocketChannel>() {
        @Override
        public void initChannel(SocketChannel ch) throws Exception {

          ch.pipeline().addLast("timeout", new IdleStateHandler(heart, heart, heart, TimeUnit.SECONDS));

          ch.pipeline().addLast(new LengthFieldPrepender(4, false));
          ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));

          ch.pipeline().addLast(transactionHandler);
        }
      });
      // Start the client.
      logger.info("connection txManager-socket-> host:" + host + ",port:" + port);
      ChannelFuture future = b.connect(host, port); // (5)
      //
      future.addListener(new ChannelFutureListener() {
        @Override
        public void operationComplete(ChannelFuture channelFuture) throws Exception {
          if (!channelFuture.isSuccess()) {
            channelFuture.channel().eventLoop().schedule(new Runnable() {
              @Override
              public void run() {
                Executors.defaultThreadFactory().newThread(() -> {
                  try {
                    close();
                    start();
                  } catch (Exception ignore) {
                  }
                }).start();
              }
            }, 5, TimeUnit.SECONDS);
          }
        }
      });

    } catch (Exception e) {
      logger.error(e.getLocalizedMessage());
    }
  }

  @Override
  public synchronized void close() {
    EventLoopGroup w = workerGroup;
    workerGroup = null;
    isStarting = false;
    SocketManager.getInstance().setNetState(false);
    if (w != null) {
      Thread thread = Executors.defaultThreadFactory().newThread(new Runnable() {
        @Override
        public void run() {
          while (!w.isShutdown()) {
            for (EventExecutor eventExecutor : w) {
              eventExecutor.shutdownGracefully();
            }
            w.shutdownGracefully();
            Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
          }
        }
      });
      thread.start();
    }
  }


  @Override
  public boolean checkState() {
    if (!SocketManager.getInstance().isNetState()) {
      logger.error("socket not connection wait 2 seconds.");
      try {
        Thread.sleep(1000 * 2);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      if (!SocketManager.getInstance().isNetState()) {
        logger.error("socket not connection,check txManager server .");
        return false;
      }
    }

    return true;
  }


  @Override
  public void destroy() throws Exception {
    close();
    SocketManager.getInstance().close();
    threadPool.shutdown();
  }
}
