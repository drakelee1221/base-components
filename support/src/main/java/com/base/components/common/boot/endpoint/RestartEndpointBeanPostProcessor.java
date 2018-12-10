package com.base.components.common.boot.endpoint;

import com.base.components.common.constants.InvasiveRewrite;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cloud.context.restart.RestartEndpoint;

/**
 * RestartEndpointBeanPostProcessor
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-07-24 9:28
 */
@InvasiveRewrite
public class RestartEndpointBeanPostProcessor implements BeanPostProcessor{

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    if((bean instanceof RestartEndpoint) && !(bean instanceof CustomRestartEndpoint)){
      return new CustomRestartEndpoint((RestartEndpoint) bean);
    }
    return bean;
  }
}
