package com.base.components.common.boot.event;

import com.base.components.common.boot.EventHandler;
import org.springframework.context.ApplicationContext;

/**
 * AbstractForcedShutdownEvent
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-07-30 11:08
 */
public abstract class AbstractForcedShutdownEvent implements EventHandler<ApplicationContext, Object> {
}
