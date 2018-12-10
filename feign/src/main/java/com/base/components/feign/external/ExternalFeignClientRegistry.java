/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.feign.external;

import com.base.components.common.boot.SpringBootApplicationRunner;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.Map;
import java.util.Set;

/**
 * OtherFeignRegistry
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-04-27 13:06
 */
public class ExternalFeignClientRegistry implements ImportBeanDefinitionRegistrar, EnvironmentAware {
  private Environment environment;
  @Override
  public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
    ClassPathScanningCandidateComponentProvider scanner = getScanner();
    scanner.addIncludeFilter(new AnnotationTypeFilter(ExternalFeignClient.class));
    Set<BeanDefinition> beanDefinitions = scanner.findCandidateComponents(
      SpringBootApplicationRunner.getProjectPackagePrefix() + "feign.**");
    for (BeanDefinition beanDefinition : beanDefinitions) {
      if (beanDefinition instanceof AnnotatedBeanDefinition) {
        AnnotatedBeanDefinition annotatedBeanDefinition = (AnnotatedBeanDefinition) beanDefinition;
        AnnotationMetadata metadata = annotatedBeanDefinition.getMetadata();
        Map<String, Object> annotationAttributes = metadata
          .getAnnotationAttributes(ExternalFeignClient.class.getCanonicalName());
        ExternalFeignClientType type = (ExternalFeignClientType) annotationAttributes.get("value");
        String name = metadata.getClassName();
        if (annotationAttributes != null) {
          name = type.getType();
        }
        BeanDefinitionBuilder definition = BeanDefinitionBuilder
          .genericBeanDefinition(ExternalFeignClientFactoryBean.class);
        definition.addPropertyValue("type", metadata.getClassName());
        definition.addPropertyValue("name", name);
        definition.addPropertyValue("errorDecoder", type.getErrorDecoder());
        definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
        String alias = name + "ExternalFeignClient";
        AbstractBeanDefinition abstractBeanDefinition = definition.getBeanDefinition();
        beanDefinition.setPrimary(true);
        BeanDefinitionHolder holder = new BeanDefinitionHolder(abstractBeanDefinition, metadata.getClassName(),
                                                               new String[] {alias}
        );
        BeanDefinitionReaderUtils.registerBeanDefinition(holder, registry);
      }
    }
  }

  private ClassPathScanningCandidateComponentProvider getScanner() {
    return new ClassPathScanningCandidateComponentProvider(false, this.environment) {
      @Override
      protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        boolean isCandidate = false;
        if (beanDefinition.getMetadata().isIndependent()) {
          if (!beanDefinition.getMetadata().isAnnotation()) {
            isCandidate = true;
          }
        }
        return isCandidate;
      }
    };
  }

  @Override
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }
}
