package com.base.components.common.boot;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

import java.lang.invoke.MethodHandles;
import java.util.Map;

/** 得到 ApplicationContext 工具类
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2017-11-21 10:31
 *
 */
public class SpringContextUtil {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private SpringContextUtil() {}

  private static final Map<String, EventHandler<ApplicationContext, ?>> STARTED_HANDLERS = Maps.newConcurrentMap();
  private static final Map<String, EventHandler<ApplicationContext, ?>> LAST_STARTED_HANDLERS = Maps.newConcurrentMap();

  private static boolean initialization;

  private static ApplicationContext applicationContext;

  /**
   * 添加应用启动完成后的事件处理
   * @param handler
   */
  public static void addStartedEvents(EventHandler<ApplicationContext, ?> handler) {
    if(EventHandler.check(handler)){
      STARTED_HANDLERS.put(handler.getId(), handler);
    }
  }

  /**
   * 添加应用启动完成后的最后事件处理
   * @param handler
   */
  public static void addLastStartedEvents(EventHandler<ApplicationContext, ?> handler) {
    if(EventHandler.check(handler)){
      LAST_STARTED_HANDLERS.put(handler.getId(), handler);
    }
  }

  /**
   * 在应用启动完成时设置Spring上下文，并触发注册的所有ApplicationStartedEventHandler
   * @param context
   */
  public static void setApplicationContextAfterStarted(ApplicationContext context) {
    applicationContext = context;
    if (applicationContext != null && !initialization) {
      initialization = true;
      for (Map.Entry<String, EventHandler<ApplicationContext, ?>> entry : STARTED_HANDLERS.entrySet()) {
        try {
          entry.getValue().onEvent(applicationContext);
        } catch (Exception e) {
          logger.error("started handler error", e);
        }
      }
      for (Map.Entry<String, EventHandler<ApplicationContext, ?>> entry : LAST_STARTED_HANDLERS.entrySet()) {
        try {
          entry.getValue().onEvent(applicationContext);
        } catch (Exception e) {
          logger.error("last started handler error", e);
        }
      }
      STARTED_HANDLERS.clear();
      LAST_STARTED_HANDLERS.clear();
    }
  }

  public static Object getBean(String beanId) {
    return applicationContext.getBean(beanId);
  }

  public static <T> T getBean(String beanId, Class<T> clazz) {
    return applicationContext.getBean(beanId, clazz);
  }

  public static <T> T getBean(Class<T> clazz) {
    return applicationContext.getBean(clazz);
  }

  public static ApplicationContext getContext() {
    return applicationContext;
  }



  /**
   * 启动完成事件 > 将 ApplicationContext 设置进 SpringContextUtil 中
   * <p>
   *   顺序：WebServerInitializedEvent > ApplicationStartedEvent > ApplicationReadyEvent
   * </p>
   */
  static class SetContextOnReadyEvent implements ApplicationListener<ApplicationReadyEvent>{
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
      logger.info("SpringContextUtil.SetContextOnReadyEvent is trigger");
      SpringContextUtil.setApplicationContextAfterStarted(event.getApplicationContext());
    }
  }

  static class RestartResetInitEvent implements ApplicationListener<ContextClosedEvent>{
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
      logger.info("SpringContextUtil.RestartResetInitEvent is trigger");
      initialization = false;
    }
  }
}
