package com.base.components.common.configuration.eureka;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cloud.netflix.eureka.EurekaClientConfigBean;

/**
 * EurekaClientConfigBeanPostProcessor
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-09-14 11:28
 */
//@Component
//@ConditionalOnClass(name = "org.springframework.cloud.netflix.eureka.EurekaClientConfigBean")
public class EurekaClientConfigBeanPostProcessor implements BeanPostProcessor {

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    if(bean instanceof EurekaClientConfigBean
      && !(bean instanceof TrimUrlBlanksEurekaClientConfigBean)){
      return new TrimUrlBlanksEurekaClientConfigBean((EurekaClientConfigBean) bean);
    }
    return bean;
  }
}
