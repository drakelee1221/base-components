package com.base.components.common.boot.event;

import com.base.components.common.tools.ProcessKiller;
import org.springframework.context.ApplicationContext;

/**
 * ProcessKillForcedShutdownEvent - 强制关闭的ShutdownEvent
 * 如需重写强制关闭的操作，请继承此类，并使用 @Component 等注解管理该 Bean 对象
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-07-30 9:48
 */
public class ProcessKillForcedShutdownEvent extends AbstractForcedShutdownEvent {

  private static final String KILL_WAIT_MS_KEY = "management.endpoint.shutdown-custom.kill-wait-ms";
  /** 强制关闭的等待时间 */
  private static final long DEFAULT_KILL_WAIT_MS = 20000;

  @Override
  public String getId() {
    return "ProcessKillForcedShutdownEvent";
  }

  @Override
  public Object onEvent(ApplicationContext applicationContext) {
    ProcessKiller.buildWithPid(getKillWaitMs(applicationContext), ProcessKiller.getCurrentPid()).kill();
    return null;
  }

  private long getKillWaitMs(ApplicationContext applicationContext) {
    try {
      return Long.valueOf(
        applicationContext.getEnvironment().getProperty(KILL_WAIT_MS_KEY, String.valueOf(DEFAULT_KILL_WAIT_MS)));
    } catch (Exception e) {
      return DEFAULT_KILL_WAIT_MS;
    }
  }
}