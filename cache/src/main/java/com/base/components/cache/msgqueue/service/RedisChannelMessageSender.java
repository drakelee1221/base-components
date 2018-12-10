package com.base.components.cache.msgqueue.service;

import com.base.components.common.boot.SpringBootApplicationRunner;
import com.base.components.common.constants.msgqueue.Channel;
import com.base.components.common.constants.msgqueue.ChannelSuffix;
import com.base.components.common.service.message.ChannelMessageSender;
import com.base.components.common.service.message.MessageEvent;
import com.base.components.common.service.message.SysMessageEventService;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.util.concurrent.Uninterruptibles;
import com.base.components.cache.msgqueue.SyncMessageEndpoint;
import com.base.components.cache.redis.RedisConnector;
import com.base.components.transaction.TransactionEvent;
import com.base.components.transaction.TransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.TimeUnit;

/**
 * 消息队列事件发送, 需要测试本地发送和接收，在profiles中加入local-msg-queue
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2017-11-15 10:07
 */
@Service
@ConditionalOnProperty({"spring.redis.host", "base.common.cache.enable"})
@ConditionalOnBean(SysMessageEventService.class)
public class RedisChannelMessageSender implements ChannelMessageSender {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  @Autowired
  private RedisTemplate redisTemplate;
  @Autowired
  private RedisConnector redisConnector;
  @Autowired
  private SysMessageEventService sysMessageEventService;
  @Value("${base.common.cache.syncMessageMaxWaitMillis:30000}")
  private Long syncMessageMaxWaitMillis;

  /**
   * 发送事件消息，需要手动管理事务，在事务提交前调用此方法
   *
   * @param messageChannel NonNull     消息Channel
   * @param eventInfoJson Nullable    业务Json
   */
  @Override
  public void sendMessage(Channel messageChannel, JsonNode eventInfoJson) {
    StackTraceElement stack = getStack();
    sendAsyncMessage(messageChannel, eventInfoJson, null, stack);
  }

  /**
   * 发送事件消息，需要手动管理事务，在事务提交前调用此方法
   *
   * @param messageChannel NonNull     消息Channel
   * @param eventInfoJson Nullable    业务Json
   * @param remark Nullable    备注
   */
  @Override
  public void sendMessage(Channel messageChannel, JsonNode eventInfoJson, String remark) {
    StackTraceElement stack = getStack();
    sendAsyncMessage(messageChannel, eventInfoJson, remark, stack);
  }

  @Override
  public <R extends Serializable> R sendSyncMessage(Channel messageChannel, JsonNode eventInfoJson) {
    StackTraceElement stack = getStack();
    return sendSyncMessage(messageChannel, eventInfoJson, null, stack);
  }

  @Override
  public <R extends Serializable> R sendSyncMessage(Channel messageChannel, JsonNode eventInfoJson, String remark) {
    StackTraceElement stack = getStack();
    return sendSyncMessage(messageChannel, eventInfoJson, remark, stack);
  }

  private void sendAsyncMessage(Channel messageChannel, JsonNode eventInfoJson, String remark,
                                StackTraceElement stack) {
    Assert.isTrue(TransactionManager.isSynchronizationActive(), "当前无事务或事务已关闭！");
    String channel = messageChannel.getId() + ChannelSuffix.getSuffix();
    MessageEvent event = sysMessageEventService.save(stack, channel, eventInfoJson, remark);
    TransactionManager.addTransactionEvent(new TransactionEvent() {
      @Override
      public void afterCompletion(int status) {
        if (status == TransactionSynchronization.STATUS_COMMITTED) {
          if (sysMessageEventService.existsById(event.getId())) {
            redisTemplate.convertAndSend(channel, event.getId());
            logger.debug("event async send > id = " + event.getId() + ", channel = " + channel);
          }
        }
      }
    });
  }

  /**
   * 同步发送事件消息，需要手动管理事务，在事务提交前调用此方法
   * <pre>
   *   每次调用会生成一个唯一的 cacheId
   *   执行步骤:  1、Sender 注册事务完成事件 > 根据事务提交状态，设置 cacheId > Sender-commit/rollback
   *             2、Sender 发布消息事件
   *             3、Sender 等待 cacheId = Listener-commit/rollback
   *             4、Listener 接收消息事件，执行子类 onAsyncMessage，方法中调用 setReturnValue 设置发送事件端的返回值
   *             5、Listener 根据 onAsyncMessage 执行是否异常，设置 cacheId > Listener-commit/rollback，
   *             6、Listener 等待 cacheId = Sender-commit/rollback
   *             7、Sender 事务完成，事件触发，见 1
   *             8、Listener 读取到 cacheId = Sender-commit/rollback，根据状态执行本地事务 commit/rollback，异常记录至数据库
   * </pre>
   */
  @SuppressWarnings("unchecked")
  private <R extends Serializable> R sendSyncMessage(Channel messageChannel, JsonNode eventInfoJson, String remark,
                                                     StackTraceElement stack) {
    Assert.isTrue(TransactionManager.isSynchronizationActive(), "当前无事务或事务已关闭！");
    String channel = messageChannel.getId() + ChannelSuffix.getSuffix();
    MessageEvent event = sysMessageEventService.saveWithNewTx(stack, channel, eventInfoJson, remark);
    String syncCacheId = SyncMessageEndpoint.buildCacheKey(event.getId());
    if (sysMessageEventService.existsByIdWithNewTx(event.getId())) {
      TransactionManager.addTransactionEvent(new TransactionEvent() {
        @Override
        public void afterCompletion(int status) {
          SyncMessageEndpoint.Status s = (status == TransactionSynchronization.STATUS_COMMITTED
                                          ? SyncMessageEndpoint.Status.COMMIT
                                          : SyncMessageEndpoint.Status.ROLLBACK);
          String v = SyncMessageEndpoint.buildCacheValue(SyncMessageEndpoint.SENDER, s);
          //主线程事务完成后通知listener提交或回滚
          redisConnector.execute((connection, serial) -> connection
            .pSetEx(serial.serialKey(syncCacheId), syncMessageMaxWaitMillis, serial.serialVal(v)));
          logger.debug("event sync sender tx over > id = " + event.getId() + ", status = " + s);
        }
      });
      redisConnector
        .execute((connection, serial) -> connection.publish(serial.serialKey(channel), serial.serialVal(syncCacheId)));
      logger.debug("event sync send > id = " + event.getId() + ", channel = " + channel);
      return (R) whileWait(syncCacheId, event.getId());
    }
    return null;
  }

  private StackTraceElement getStack() {
    StackTraceElement[] array = Thread.currentThread().getStackTrace();
    String thisClassName = RedisChannelMessageSender.class.getName();
    for (StackTraceElement stack : array) {
      String cn = stack.getClassName();
      if (cn.startsWith(SpringBootApplicationRunner.getProjectPackagePrefix()) && !cn.startsWith(thisClassName)) {
        return stack;
      }
    }
    return array[0];
  }

  private Object whileWait(String cacheId, String eventId) {
    long c = System.currentTimeMillis();
    while (true) {
      if (System.currentTimeMillis() - c > syncMessageMaxWaitMillis) {
        throw new MessageChannelException("MessageSender wait Listener commit timeout !");
      }
      Object v = redisConnector
        .execute((connection, serial) -> serial.deserialVal(connection.get(serial.serialKey(cacheId))));
      SyncMessageEndpoint endpoint = SyncMessageEndpoint.getEndpoint(v);
      if (endpoint != null && SyncMessageEndpoint.LISTENER.equals(endpoint)) {
        Object o = redisConnector.execute((connection, serial) -> serial
          .deserialVal(connection.get(serial.serialKey(SyncMessageEndpoint.buildReturnValueCacheKey(eventId)))));
        //获取返回值
        SyncMessageEndpoint.Status status = SyncMessageEndpoint.getStatus(v);
        if (SyncMessageEndpoint.Status.EXCEPTION.equals(status)) {
          throw new MessageChannelException(o != null ? o.toString() : "MessageListener Exception !");
        }
        return o;
      }
      Uninterruptibles.sleepUninterruptibly(50, TimeUnit.MILLISECONDS);
    }
  }
}
