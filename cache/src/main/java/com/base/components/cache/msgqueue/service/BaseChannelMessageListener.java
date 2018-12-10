package com.base.components.cache.msgqueue.service;

import com.base.components.common.constants.msgqueue.Channel;
import com.base.components.common.log.BaseLogFilter;
import com.base.components.common.service.message.MessageEvent;
import com.base.components.common.service.message.SysMessageEventService;
import com.base.components.common.util.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Throwables;
import com.google.common.util.concurrent.Uninterruptibles;
import com.base.components.cache.msgqueue.BaseRedisChannelMessageListener;
import com.base.components.cache.msgqueue.SyncMessageEndpoint;
import com.base.components.cache.redis.RedisConnector;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.TimeUnit;

/**
 * 消息接收监听抽象类，所有需要监听的自定义服务需要继承此类
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2017-11-15 11:34
 */
abstract class BaseChannelMessageListener extends BaseRedisChannelMessageListener {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /**
   * 同步事件最大等待超时时间
   */
  @Value("${base.common.cache.syncMessageMaxWaitMillis:30000}")
  private Long syncMessageMaxWaitMillis;

  @Autowired
  private PlatformTransactionManager transactionManager;

  @Autowired
  private SysMessageEventService sysMessageEventService;

  @Autowired
  private RedisConnector redisConnector;

  @Value("${spring.application.name}")
  private String serverName;

  @Override
  protected final void onMessage(Serializable messageContent, String messageChannel) {
    String completeId = messageContent.toString();
    boolean sync = SyncMessageEndpoint.isSyncCacheKey(completeId);
    String id = sync ? SyncMessageEndpoint.cleanSyncPrefix(completeId) : completeId;
    MessageEvent event = sysMessageEventService.findById(id);
    Channel channel = Channel.parse(messageChannel);
    Assert.notNull(channel, "Channel parse error > " + messageChannel + ", id = " + id);
    if (event != null) {
      String handleId = BaseLogFilter.ignoreLogging(() -> {
        try {
          return sysMessageEventService.createHandle(id, buildListenId(sync, channel), getClass().getName());
        } catch (Exception ignore) {
          return null;
        }
      });
      if (handleId != null) {
        //只有发送同步消息，且当前是同步监听器才会调用同步方法
        if (sync && (this instanceof BaseSyncChannelMessageListener)) {
          sync(channel, event, handleId, true);
        }
        //其它全部触发异步方法
        else {
          async(channel, event, handleId, sync);
        }
        return;
      }
    }
    logger.debug("Listener[{}], event is null or invoke on other server ! id = {}", getClass().getName(), id);
  }

  private void async(Channel channel, MessageEvent event, String handleId, boolean isSyncSender) {
    String error = null;
    DefaultTransactionDefinition def = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    TransactionStatus transaction = transactionManager.getTransaction(def);
    try {
      logger.debug("async event onAsyncMessage begin - " + event.getId());
      onAsyncMessage(
        channel, StringUtils.isBlank(event.getEventInfoJson())
                 ? null
                 : JsonUtils.reader(event.getEventInfoJson(), JsonNode.class), event.getRemark(), isSyncSender);
      transactionManager.commit(transaction);
    } catch (Exception e) {
      logger.error("async event onAsyncMessage error !", e);
      error = getExceptionStack(e);
      if (!transaction.isCompleted()) {
        transactionManager.rollback(transaction);
      }
    }
    try {
      sysMessageEventService.updateDoing(handleId, channel.getId(), error);
    } catch (Exception e) {
      logger.error("async event updateDoing error !!!", e);
    }
    logger.debug("async event onAsyncMessage over - " + event.getId());
  }

  private void sync(Channel channel, MessageEvent event, String handleId, boolean isSyncSender) {
    String error = null;
    String cacheId = SyncMessageEndpoint.buildCacheKey(event.getId());
    DefaultTransactionDefinition def = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    TransactionStatus transaction = transactionManager.getTransaction(def);
    try {
      logger.debug("sync event onAsyncMessage begin - " + event.getId());
      Serializable returnVal = onSyncMessage(channel, StringUtils.isBlank(event.getEventInfoJson())
                                                      ? null
                                                      : JsonUtils.reader(event.getEventInfoJson(), JsonNode.class),
                                             event.getRemark(), isSyncSender
      );
      redisConnector.execute((connection, serial) -> connection
        .pSetEx(serial.serialKey(SyncMessageEndpoint.buildReturnValueCacheKey(event.getId())), syncMessageMaxWaitMillis,
                serial.serialVal(returnVal)
        ));
      //通知sender，设置listener wait
      redisConnector.execute((connection, serial) -> connection
        .pSetEx(serial.serialKey(cacheId), syncMessageMaxWaitMillis, serial.serialVal(
          SyncMessageEndpoint.buildCacheValue(SyncMessageEndpoint.LISTENER, SyncMessageEndpoint.Status.WAITING))));
      whileWait(cacheId, transaction);
    } catch (Exception e) {
      logger.error("sync event onAsyncMessage error !", e);
      error = getExceptionStack(e);
      if (!transaction.isCompleted()) {
        transactionManager.rollback(transaction);
      }
      // 不是在等待Sender抛出的异常，则通知sender
      if (!(e instanceof MessageChannelException)) {
        try {
          //通知sender，设置返回值 - 异常信息
          redisConnector.execute((connection, serial) -> connection
            .pSetEx(serial.serialKey(SyncMessageEndpoint.buildReturnValueCacheKey(event.getId())),
                    syncMessageMaxWaitMillis, serial.serialVal(e.getMessage())
            ));
          //通知sender，设置listener exception
          redisConnector.execute((connection, serial) -> connection
            .pSetEx(serial.serialKey(cacheId), syncMessageMaxWaitMillis, serial.serialVal(SyncMessageEndpoint
                                                                                            .buildCacheValue(
                                                                                              SyncMessageEndpoint.LISTENER,
                                                                                              SyncMessageEndpoint.Status.EXCEPTION
                                                                                            ))));
        } catch (Exception e1) {
          error = getExceptionStack(e1);
          logger.error("sync event notify Sender > Listener is exception, but redis error !", e1);
        }
      }
    }
    try {
      sysMessageEventService.updateDoing(handleId, channel.getId(), error);
    } catch (Exception e) {
      logger.error("sync event updateDoing error !!!", e);
    }
    logger.debug("sync event onAsyncMessage over - " + event.getId());
  }

  private String buildListenId(boolean sync, Channel channel) {
    if (sync && (this instanceof BaseSyncChannelMessageListener)) {
      return channel.getId() + Channel.SPLIT_HANDLE_SYNC;
    } else {
      return getClass().getName() + Channel.SPLIT_HANDLE + serverName;
    }
  }

  /**
   * 接收消息异步执行方法，请将异常抛出，待后续执行；如果未抛出异常，则认为方法执行成功。<br/>
   * 默认开起事务传播级别 {@link TransactionDefinition#PROPAGATION_REQUIRED}
   *
   * @param messageChannel NonNull     消息Channel
   * @param eventInfoJson Nullable    发送者发送的业务Json
   * @param remark Nullable    发送者传入的备注信息
   * @param isSyncSender NonNull    发送者是否调用的是同步发送，true=是，false=false
   *
   * @throws Exception -
   */
  protected abstract void onAsyncMessage(Channel messageChannel, JsonNode eventInfoJson, String remark, boolean isSyncSender)
    throws Exception;

  /**
   * 接收消息同步执行方法，请将异常抛出，待后续执行；如果未抛出异常，则认为方法执行成功。<br/>
   * 默认开起事务传播级别 {@link TransactionDefinition#PROPAGATION_REQUIRED}
   *
   * @param messageChannel NonNull     消息Channel
   * @param eventInfoJson Nullable    发送者发送的业务Json
   * @param remark Nullable    发送者传入的备注信息
   * @param isSyncSender NonNull    发送者是否调用的是同步发送，true=是，false=false
   *
   * @throws Exception -
   */
  protected abstract Serializable onSyncMessage(Channel messageChannel, JsonNode eventInfoJson, String remark, boolean isSyncSender)
    throws Exception;

  private void whileWait(String cacheId, TransactionStatus transaction) {
    long c = System.currentTimeMillis();
    while (true) {
      if (System.currentTimeMillis() - c > syncMessageMaxWaitMillis) {
        throw new MessageChannelTimeoutException("MessageListener wait Sender commit timeout !");
      }
      Object v = redisConnector
        .execute((connection, serial) -> serial.deserialVal(connection.get(serial.serialKey(cacheId))));
      SyncMessageEndpoint endpoint = SyncMessageEndpoint.getEndpoint(v);
      if (endpoint != null && SyncMessageEndpoint.SENDER.equals(endpoint)) {
        SyncMessageEndpoint.Status status = SyncMessageEndpoint.getStatus(v);
        try {
          if (SyncMessageEndpoint.Status.COMMIT.equals(status)) {
            transactionManager.commit(transaction);
          } else {
            transactionManager.rollback(transaction);
          }
        } catch (Exception e) {
          logger.error("MessageListener waiting Sender was " + status + ",  Listener " + status + " exception !");
          throw new MessageChannelException(e);
        }
        logger.debug("MessageListener waiting Sender was " + status + ",  Listener to " + status + " !");
        return;
      }
      Uninterruptibles.sleepUninterruptibly(50, TimeUnit.MILLISECONDS);
    }
  }

  private String getExceptionStack(Exception e) {
    String error = Throwables.getStackTraceAsString(e);
    return error.length() > 2000 ? error.substring(0, 1999) : error;
  }
}
