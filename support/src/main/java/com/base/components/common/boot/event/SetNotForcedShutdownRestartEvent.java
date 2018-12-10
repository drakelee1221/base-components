package com.base.components.common.boot.event;

import com.base.components.common.boot.endpoint.CustomShutdownEndpoint;
import org.springframework.context.ApplicationContext;

/**
 * SetNotForcedShutdownRestartEvent
 * 设置标识为非强制关闭的RestartEvent
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-07-30 9:47
 */
public class SetNotForcedShutdownRestartEvent extends AbstractRestartEvent {
  @Override
  public String getId() {
    return "SetNotForcedShutdownRestartEvent";
  }

  @Override
  public Object onEvent(ApplicationContext applicationContext) {
    CustomShutdownEndpoint.FORCED_SHUTDOWN.set(Boolean.FALSE);
    return null;
  }
}
