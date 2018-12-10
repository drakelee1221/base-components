package com.base.components.common.boot.endpoint;

import com.base.components.common.boot.EventHandler;
import com.base.components.common.boot.event.AbstractRestartEvent;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.cloud.context.restart.RestartEndpoint;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.Map;

/**
 * CustomRestartEndpoint
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-07-24 9:35
 */
@Endpoint(id = "restart", enableByDefault = false)
public class CustomRestartEndpoint extends RestartEndpoint {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final Map<String, EventHandler<ApplicationContext, ?>> RESTART_EVENTS = Maps.newConcurrentMap();
  private RestartEndpoint delegate;
  private ConfigurableApplicationContext context;

  public CustomRestartEndpoint(RestartEndpoint delegate) {
    this.delegate = delegate;
  }

  @Override
  public long getTimeout() {
    return delegate.getTimeout();
  }

  @Override
  public void setTimeout(long timeout) {
    delegate.setTimeout(timeout);
  }

  @Override
  public void setIntegrationMBeanExporter(Object exporter) {
    delegate.setIntegrationMBeanExporter(exporter);
  }

  @Override
  public void onApplicationEvent(ApplicationPreparedEvent event) {
    if (this.context == null) {
      this.context = event.getApplicationContext();
    }
    delegate.onApplicationEvent(event);
  }

  @Override
  @WriteOperation
  public Object restart() {
    Object obj = checkConditions();
    if(obj != null){
      return obj;
    }
    try {
      RESTART_EVENTS.forEach((id, event) -> event.onEvent(context));
    } catch (Exception e) {
      logger.error("CustomRestartEndpoint on restart event error", e);
    }
    return delegate.restart();
  }

  private Object checkConditions(){
    if(context != null){
      try {
        Map<String, RestartCondition> conditionMap = context.getBeansOfType(RestartCondition.class);
        for (Map.Entry<String, RestartCondition> entry : conditionMap.entrySet()) {
          RestartCondition c = entry.getValue();
          if(!c.apply()){
            return Collections.singletonMap("message", c.reason());
          }
        }
      } catch (Exception ignore) {
      }
    }
    return null;
  }

  @Override
  public PauseEndpoint getPauseEndpoint() {
    return delegate.getPauseEndpoint();
  }

  @Override
  public ResumeEndpoint getResumeEndpoint() {
    return delegate.getResumeEndpoint();
  }

  @Override
  public ConfigurableApplicationContext doRestart() {
    return delegate.doRestart();
  }

  @Override
  public boolean isRunning() {
    return delegate.isRunning();
  }

  @Override
  public void doPause() {
    delegate.doPause();
  }

  @Override
  public void doResume() {
    delegate.doResume();
  }

  /**
   * 注册重启事件
   *
   * @param restartEvent -
   */
  public static void addRestartEvent(AbstractRestartEvent restartEvent) {
    if (EventHandler.check(restartEvent)) {
      RESTART_EVENTS.put(restartEvent.getId(), restartEvent);
    }
  }
}
