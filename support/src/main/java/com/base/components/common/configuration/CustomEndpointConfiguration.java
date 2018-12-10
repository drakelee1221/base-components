package com.base.components.common.configuration;

import com.base.components.common.boot.endpoint.CustomRestartEndpoint;
import com.base.components.common.boot.endpoint.CustomShutdownEndpoint;
import com.base.components.common.boot.endpoint.RestartEndpointBeanPostProcessor;
import com.base.components.common.boot.event.AbstractForcedShutdownEvent;
import com.base.components.common.boot.event.AbstractRestartEvent;
import com.base.components.common.boot.event.ProcessKillForcedShutdownEvent;
import com.base.components.common.boot.event.SetNotForcedShutdownRestartEvent;
import com.base.components.common.boot.event.ShutdownOffLogLevelEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * CustomEndpointConfiguration
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-07-24 9:55
 */
@Configuration
@ConditionalOnProperty(name = "management.endpoint.shutdown-custom.enabled", havingValue = "true", matchIfMissing = true)
public class CustomEndpointConfiguration{
  @Autowired
  private List<AbstractForcedShutdownEvent> abstractShutdownEvents;

  @Autowired
  private List<AbstractRestartEvent> abstractRestartEvents;

  @PostConstruct
  public void addEvent() {
    abstractRestartEvents.forEach(CustomRestartEndpoint::addRestartEvent);
    abstractShutdownEvents.forEach(CustomShutdownEndpoint::addForcedShutdownEvent);
  }

  @Bean
  public RestartEndpointBeanPostProcessor restartEndpointBeanPostProcessor() {
    return new RestartEndpointBeanPostProcessor();
  }

  @Bean
  public CustomShutdownEndpoint customShutdownEndpoint() {
    return new CustomShutdownEndpoint();
  }

  @Bean
  public SetNotForcedShutdownRestartEvent setNotForcedShutdownRestartEvent() {
    return new SetNotForcedShutdownRestartEvent();
  }

  @Bean
  public ProcessKillForcedShutdownEvent processKillForcedShutdownEvent() {
    return new ProcessKillForcedShutdownEvent();
  }

  @Bean
  public ShutdownOffLogLevelEvent shutdownOffLogLevelEvent(){
    return new ShutdownOffLogLevelEvent();
  }
}
