package com.base.components.common.boot.endpoint;

import com.base.components.common.boot.EventHandler;
import com.base.components.common.boot.event.AbstractForcedShutdownEvent;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.boot.actuate.context.ShutdownEndpoint;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.context.ApplicationContext;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * CustomShutdownEndpoint
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-07-23 16:59
 */
@Endpoint(id = "shutdown", enableByDefault = false)
public class CustomShutdownEndpoint extends ShutdownEndpoint {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final Map<String, EventHandler<ApplicationContext, ?>> FORCED_SHUTDOWN_EVENTS = Maps.newConcurrentMap();
  /** true = 直接请求的shutdown操作，false = 可能由其它操作附带的shutdown操作（如：restart） */
  public static final AtomicBoolean FORCED_SHUTDOWN = new AtomicBoolean(Boolean.TRUE);
  private ApplicationContext context;

  @Override
  @WriteOperation
  public Map<String, String> shutdown() {
    if (FORCED_SHUTDOWN.get()) {
      try {
        FORCED_SHUTDOWN_EVENTS.forEach((id, event) -> event.onEvent(context));
        FORCED_SHUTDOWN_EVENTS.clear();
      } catch (Exception e) {
        logger.error("CustomShutdownEndpoint on forced shutdown event error", e);
      }
    } else {
      FORCED_SHUTDOWN.set(Boolean.TRUE);
    }
    return super.shutdown();
  }

  @Override
  public void setApplicationContext(ApplicationContext context) throws BeansException {
    this.context = context;
    super.setApplicationContext(context);
  }

  /**
   * 注册强制关闭事件，每个事件只会触发一次，只会在shutdown时触发（restart 不会触发）
   *
   * @param forcedShutdownEvent -
   */
  public static void addForcedShutdownEvent(AbstractForcedShutdownEvent forcedShutdownEvent) {
    if (EventHandler.check(forcedShutdownEvent)) {
      FORCED_SHUTDOWN_EVENTS.put(forcedShutdownEvent.getId(), forcedShutdownEvent);
    }
  }
}
