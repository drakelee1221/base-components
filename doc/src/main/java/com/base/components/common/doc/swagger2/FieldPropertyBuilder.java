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

import com.base.components.common.doc.annotation.Param;
import com.base.components.common.doc.type.ArrayType;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import com.google.common.base.Optional;
import org.springframework.core.annotation.AnnotationUtils;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.ModelPropertyBuilderPlugin;
import springfox.documentation.spi.schema.contexts.ModelPropertyContext;
import springfox.documentation.swagger.common.SwaggerPluginSupport;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

import static springfox.documentation.schema.Annotations.findPropertyAnnotation;

/**
 * FieldPropertyBuilder - 读取字段信息
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-08-24 15:52
 */
public class FieldPropertyBuilder implements ModelPropertyBuilderPlugin {

  @Override
  public void apply(ModelPropertyContext context) {
    Optional<Param> annotation = Optional.absent();

    if (context.getAnnotatedElement().isPresent()) {
      annotation = annotation.or(findApiModePropertyAnnotation(context.getAnnotatedElement().get()));
    }
    if (context.getBeanPropertyDefinition().isPresent()) {
      annotation = annotation.or(findPropertyAnnotation(
          context.getBeanPropertyDefinition().get(),
          Param.class));
    }
    if (annotation.isPresent()) {
      Param field = annotation.get();
      context.getBuilder()
//          .allowableValues(allowableValueFromString(""))
          .required(field.required())
          .description(field.value())
          .isHidden(field.hidden())
//          .type(type(context.getResolver(), field))
          .example(ExampleHelper.parseExample(context.getBuilder().build().getType().getErasedType(), field.example()));
    }
  }
  private ResolvedType type(TypeResolver typeResolver, Param field){
    ResolvedType type;
    if(field.dataType().equals(ArrayType.class)){
      type = typeResolver.resolve(field.dataType(), field.genericType());
    }
    else{
      type = typeResolver.resolve(field.dataType());
    }
    return type;
  }

  private Optional<Param> findApiModePropertyAnnotation(AnnotatedElement annotated) {
    Optional<Param> annotation = Optional.absent();

    if (annotated instanceof Method) {
      // If the annotated element is a method we can use this information to check superclasses as well
      annotation = Optional.fromNullable(AnnotationUtils.findAnnotation(((Method) annotated), Param.class));
    }

    return annotation.or(Optional.fromNullable(AnnotationUtils.getAnnotation(annotated, Param.class)));
  }

  @Override
  public boolean supports(DocumentationType delimiter) {
    return SwaggerPluginSupport.pluginDoesApply(delimiter);
  }
}
