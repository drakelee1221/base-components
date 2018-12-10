package com.base.components.common.boot.event;

import com.base.components.common.boot.EventHandler;
import org.springframework.context.ApplicationContext;

/**
 * AbstractRestartEvent
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-07-30 11:06
 */
public abstract class AbstractRestartEvent implements EventHandler<ApplicationContext, Object> {
}
