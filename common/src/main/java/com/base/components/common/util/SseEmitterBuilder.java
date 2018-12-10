package com.base.components.common.util;

import com.base.components.common.exception.ExceptionIdCreator;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.Uninterruptibles;
import com.base.components.common.boot.SpringContextUtil;
import com.base.components.common.exception.ExceptionResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * SseEmitterBuilder，【在 Servlet 环境下使用】
 * SSE 返回对象构建
 * <pre>
 * {@code
 * --------- 后端 Example ---------
 * GetMapping(value = "/xxx", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
 * public SseEmitter eventSteam(@RequestParam Map<String, String> params, HttpServletResponse response) throws IOException {
 *   //主线程
 *   return SseEmitterBuilder.buildWhileSendAndNoTimeout((sendStatus, mainThreadVariables) -> {
 *     //异步子线程
 *     //sendStatus.complete(); //停止发送
 *     //Printer.err("sendCount = " + sendStatus.sendCount());
 *     return "{\"info\":\"" + params + "\"}";
 *   }, threadPoolService.getExecutor());
 * }
 *
 * --------- 前端 Example ---------
 * var source = new EventSource('http://host/xxx?a=1');
 * source.addEventListener('message', function (e) {
 *   console.info(e.data);
 *   //do something...
 * });
 *
 * source.addEventListener('complete', function(e) {
 *   console.log(e.data);
 *   this.close();
 * });
 *
 * source.addEventListener('error', function (e) {
 *   console.error(e.data);
 *   //if (e.eventPhase === EventSource.CLOSED) {
 *   this.close();
 *   //}
 * });
 *
 *
 * }</pre>
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-07-11 16:07
 */
public abstract class SseEmitterBuilder {

  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  public static final String TRACE_ID_KEY = "_TRACE_ID_";
  private static final long ZERO = 0L;
  /** 主线程变量，当前临时，通过 {@link #addCurrentMaimThreadVariable(String, Object)} 在主线程设置*/
  private static final InheritableThreadLocal<MainThreadVariables> MAIM_THREAD_VARIABLES = new InheritableThreadLocal<>();
  /** 主线程变量，值获取函数 */
  private static final Map<String, Supplier> MAIM_THREAD_VARIABLES_SUPPLIERS = Maps.newHashMap();

  static {
    //添加追踪ID变量
    MAIM_THREAD_VARIABLES_SUPPLIERS.put(TRACE_ID_KEY, SseEmitterBuilder::getTraceId);
  }

  /**
   * 一直循环向前端发送 event-stream 消息，前端终止前该方法不会自己停止或超时（默认间隔两千毫秒向前端发送一次消息），
   * 完成时会发送 complete 事件
   *
   * @param messageFunction 发送消息体的方法
   * @param executor 异步调用器
   *
   * @return Controller 直接返回该对象
   */
  public static SseEmitter buildWhileSendAndNoTimeout(BiFunction<SendStatus, MainThreadVariables, Object> messageFunction, Executor executor) {
    return build(new DefaultWhileSender(messageFunction, ZERO), ZERO, executor);
  }

  /**
   * 一直循环向前端发送 event-stream 消息，前端终止前该方法不会自己停止或超时，
   * 完成时会发送 complete 事件
   *
   * @param messageFunction 发送消息体的方法
   * @param sleepMilliseconds 间隔多少毫秒向前端发送一次消息
   * @param executor 异步调用器
   *
   * @return Controller 直接返回该对象
   */
  public static SseEmitter buildWhileSendAndNoTimeout(BiFunction<SendStatus, MainThreadVariables, Object> messageFunction,
                                                      long sleepMilliseconds, Executor executor) {
    return build(new DefaultWhileSender(messageFunction, sleepMilliseconds), ZERO, executor);
  }

  /**
   * 向前端发送 event-stream 消息，前端终止前该方法不会超时
   *
   * @param sendConsumer 发送消息的具体策略实现，参考{@link DefaultWhileSender}
   * @param executor 异步调用器
   *
   * @return Controller 直接返回该对象
   */
  @Deprecated
  public static SseEmitter buildNoTimeout(BiConsumer<SseEmitter, MainThreadVariables> sendConsumer, Executor executor) {
    return build(sendConsumer, ZERO, executor);
  }

  /**
   * 向前端发送 event-stream 消息
   *
   * @param sendConsumer 发送消息的具体策略实现，参考{@link DefaultWhileSender}
   * @param timeout 超时时间（单位毫秒）
   * @param executor 异步调用器
   *
   * @return Controller 直接返回该对象
   */
  @Deprecated
  public static SseEmitter buildWithTimeout(BiConsumer<SseEmitter, MainThreadVariables> sendConsumer, long timeout,
                                            Executor executor) {
    Assert.isTrue(timeout > ZERO, "timeout must greater than 0");
    return build(sendConsumer, timeout, executor);
  }

  private static SseEmitter build(BiConsumer<SseEmitter, MainThreadVariables> sendConsumer, long timeout,
                                  Executor executor) {
    SseEmitter sseEmitter = new SseEmitter(timeout);
    executor.execute(() -> sendConsumer.accept(sseEmitter, MainThreadVariables.build()));
    MAIM_THREAD_VARIABLES.remove();
    return sseEmitter;
  }

  /** 添加当前主线程临时变量 */
  public static void addCurrentMaimThreadVariable(String key, Object value) {
    if (key != null && value != null) {
      MainThreadVariables variables = MAIM_THREAD_VARIABLES.get();
      if (variables == null) {
        variables = new MainThreadVariables();
        MAIM_THREAD_VARIABLES.set(variables);
      }
      variables.put(key, value);
    }
  }

  /**
   * 循环发送器，会在结束时发送 complete 事件，异常时发送异常对象
   */
  public static class DefaultWhileSender implements BiConsumer<SseEmitter, MainThreadVariables> {
    private static final long DEFAULT_SLEEP_MILLISECONDS = 2000L;
    private SendStatus sendStatus = new SendStatus();
    private BiFunction<SendStatus, MainThreadVariables, Object> messageFunction;
    private long sleepMilliseconds;

    public DefaultWhileSender(BiFunction<SendStatus, MainThreadVariables, Object> messageFunction, long sleepMilliseconds) {
      this.messageFunction = messageFunction;
      this.sleepMilliseconds = (sleepMilliseconds > ZERO ? sleepMilliseconds : DEFAULT_SLEEP_MILLISECONDS);
    }

    @Override
    public void accept(SseEmitter sseEmitter, MainThreadVariables mainThreadVariables) {
      try {
        while (sendStatus.continueSend.get()) {
          Object message = messageFunction.apply(sendStatus, mainThreadVariables);
          if (sendStatus.continueSend.get()) {
            sseEmitter.send(message);
            sendStatus.sendCount.incrementAndGet();
          } else {
            //发送完成事件
            sseEmitter.send(SseEmitter.event().name("complete")
                                      .data(sendStatus.completeMessage == null ? "" : sendStatus.completeMessage));
            return;
          }
          Uninterruptibles.sleepUninterruptibly(sleepMilliseconds, TimeUnit.MILLISECONDS);
        }
      } catch (Exception e) {
        Object traceId = mainThreadVariables.get(TRACE_ID_KEY);
        if(e instanceof IOException){
          logger.warn("[event-stream] send event error (TraceId: " + traceId + "), " + Throwables.getRootCause(e).getMessage());
        }
        else{
          logger.error("[event-stream] send event error (TraceId: " + traceId + ")", e);
          try {
            ExceptionResponseEntity error = new ExceptionResponseEntity(
              e, true, null,  getAppName(), traceId == null ? null : traceId.toString(), null, null);
            sseEmitter.send(SseEmitter.event().name("error").data(error));
          } catch (IOException e1) {
            logger.warn("[event-stream] send error fail (TraceId: " + traceId + "), " + e1.getMessage());
          }
        }
      }
      sseEmitter.complete();
    }
  }

  public static class SendStatus {
    private AtomicInteger sendCount = new AtomicInteger(0);
    private AtomicBoolean continueSend = new AtomicBoolean(Boolean.TRUE);
    private Object completeMessage;
    /**
     * 设置发送完成，将停止向前端推送信息
     */
    public void complete(Object completeMessage) {
      this.completeMessage = completeMessage;
      continueSend.set(Boolean.FALSE);
    }

    /**
     * 设置发送完成，将停止向前端推送信息
     */
    public void complete() {
      continueSend.set(Boolean.FALSE);
    }
    /**
     * 获取发送次数
     */
    public int sendCount() {
      return sendCount.get();
    }
  }

  /**
   * 主线程变量
   */
  public static class MainThreadVariables extends ConcurrentHashMap<String, Object> {
    private static final long serialVersionUID = -1662331034499537225L;

    private MainThreadVariables() {
    }

    private static MainThreadVariables build(){
      MainThreadVariables map = new MainThreadVariables();
      for (Entry<String, Supplier> entry : MAIM_THREAD_VARIABLES_SUPPLIERS.entrySet()) {
        Object val = entry.getValue().get();
        if(val != null){
          map.put(entry.getKey(), val);
        }
      }
      MainThreadVariables variables = MAIM_THREAD_VARIABLES.get();
      if(variables != null && !variables.isEmpty()){
        map.putAll(variables);
      }
      MAIM_THREAD_VARIABLES.remove();
      return map;
    }
  }

  private static String getTraceId() {
    try {
      ExceptionIdCreator idCreator = SpringContextUtil.getBean(ExceptionIdCreator.class);
      return idCreator.getCurrentTraceId();
    } catch (Exception ignore) {
      return null;
    }
  }
  private static String getAppName() {
    try {
      Environment env = SpringContextUtil.getBean(Environment.class);
      return env.getProperty("spring.application.name");
    } catch (Exception ignore) {
      return null;
    }
  }

}
