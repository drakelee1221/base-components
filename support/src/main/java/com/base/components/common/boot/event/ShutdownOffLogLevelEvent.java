package com.base.components.common.boot.event;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.base.components.common.boot.SpringBootApplicationRunner;
import com.google.common.collect.Sets;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.Set;

/**
 * ShutdownOffLogLevelEvent - 强制关闭时，忽略部分日志
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-07-24 14:40
 */
public class ShutdownOffLogLevelEvent extends AbstractForcedShutdownEvent {
  private Set<String> offLogClass = Sets.newHashSet(
    "org.springframework.context.annotation.AnnotationConfigApplicationContext",
    "org.springframework.cloud.client.discovery.health"
  );

  @Override
  public String getId() {
    return ShutdownOffLogLevelEvent.class.getName();
  }

  @Override
  public Object onEvent(ApplicationContext applicationContext) {
    LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
    for (String logClass : offLogClass) {
      try {
        loggerContext.getLogger(logClass).setLevel(Level.OFF);
      } catch (Exception ignore) {
      }
    }
    try {
      if (!getClass().getName().startsWith(SpringBootApplicationRunner.getProjectPackagePrefix())) {
        loggerContext.getLogger(SpringBootApplicationRunner.getProjectPackagePrefix()).setLevel(Level.OFF);
      }
    } catch (Exception ignore) {
    }
    return null;
  }
}
