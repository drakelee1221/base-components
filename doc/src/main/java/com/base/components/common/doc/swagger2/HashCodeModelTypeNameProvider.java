/*
 *
 *  Copyright 2015 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package com.base.components.common.doc.swagger2;

import io.swagger.annotations.ApiModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import springfox.documentation.schema.DefaultTypeNameProvider;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.swagger.common.SwaggerPluginSupport;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Strings.emptyToNull;
import static com.base.components.common.doc.DocConstants.DYNAMIC_BEAN_SPLIT;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

/**
 * AAModelTypeNameProvider - 命名规则
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-08-23 14:02
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Primary
@ConditionalOnProperty(name = "api-doc.swagger.enabled", havingValue = "true")
public class HashCodeModelTypeNameProvider extends DefaultTypeNameProvider {
  @Override
  public String nameFor(Class<?> type) {
    ApiModel annotation = findAnnotation(type, ApiModel.class);
    String defaultTypeName = super.nameFor(type) + DYNAMIC_BEAN_SPLIT + type.hashCode();
    if (annotation != null) {
      return fromNullable(emptyToNull(annotation.value())).or(defaultTypeName);
    }
    return defaultTypeName;
  }

  @Override
  public boolean supports(DocumentationType delimiter) {
    return SwaggerPluginSupport.pluginDoesApply(delimiter);
  }
}
