/*
 *
 *  Copyright 2015-2019 the original author or authors.
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
import com.base.components.common.doc.annotation.RequestBodyModel;
import com.base.components.common.doc.annotation.RequestModel;
import com.base.components.common.doc.annotation.Token;
import com.fasterxml.classmate.TypeResolver;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.schema.Collections;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.schema.ModelReference;
import springfox.documentation.schema.Types;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.contexts.OperationContext;
import springfox.documentation.swagger.common.SwaggerPluginSupport;

import java.util.Collection;
import java.util.List;

import static com.base.components.common.doc.DocConstants.DYNAMIC_BEAN_SPLIT;
import static springfox.documentation.swagger.common.SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER;

/**
 * OperationRequestParamReader - 读取请求参数
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-08-22 14:00
 */
public class OperationRequestParamReader implements OperationBuilderPlugin {
  @Autowired
  private TypeResolver typeResolver;

  @Override
  public void apply(OperationContext context) {
    context.operationBuilder().parameters(readParameters(context));
  }

  @Override
  public boolean supports(DocumentationType delimiter) {
    return SwaggerPluginSupport.pluginDoesApply(delimiter);
  }

  private Parameter readRequestBodyModel(RequestBodyModel param) {
    ModelReference modelRef = new ModelRef(RequestBodyModel.class.getSimpleName() + DYNAMIC_BEAN_SPLIT + param.hashCode());
    if(param.isCollection()){
      modelRef = new ModelRef(Collections.containerType(typeResolver.resolve(Collection.class)), modelRef);
    }
    return new ParameterBuilder()
        .name(param.name())
        .description(param.description())
        .required(param.required())
        .modelRef(modelRef)
        .parameterType("body")
        .order(SWAGGER_PLUGIN_ORDER)
        .build();
  }

  private List<Parameter> readRequestModel(RequestModel model, String handlerName) {
    List<Parameter> parameters = Lists.newArrayList();
    for (Param field : model.value()) {
      Assert.hasText(field.name(), handlerName + " > Param name is null !");
      parameters.add(new ParameterBuilder()
        .name(field.name())
        .description(field.value())
        .required(field.required())
        .modelRef(new ModelRef(type(field.dataType())))
        .parameterType(field.requestScope().toString().toLowerCase())
        .order(SWAGGER_PLUGIN_ORDER)
        .scalarExample(ExampleHelper.parseExample(field.dataType(), field.example()))
        .build());
    }
    return parameters;
  }

  private Parameter readToken(Token token, String handlerName) {
    Assert.hasText(token.name(), handlerName + " > Token name is null !");
    return new ParameterBuilder()
                     .name(token.name())
                     .description(token.value())
                     .required(token.require())
                     .modelRef(new ModelRef(type(String.class)))
                     .parameterType(token.tokenScope().toString().toLowerCase())
                     .order(SWAGGER_PLUGIN_ORDER)
                     .build();
  }

  private String type(Class dataType){
    String s = Types.typeNameFor(dataType);
    if(s == null){
      s = dataType.getName();
    }
    return s;
  }


  private List<Parameter> readParameters(OperationContext context) {
    List<Parameter> parameters = Lists.newArrayList();
    Optional<RequestBodyModel> requestBodyModel = context.findAnnotation(RequestBodyModel.class);
    if (requestBodyModel.isPresent()) {
      parameters.add(readRequestBodyModel(requestBodyModel.get()));
    }
    Optional<RequestModel> requestModel = context.findAnnotation(RequestModel.class);
    if (requestModel.isPresent()) {
      parameters.addAll(readRequestModel(requestModel.get(), context.getName()));
    }
    Optional<Token> token = context.findAnnotation(Token.class);
    if (token.isPresent()) {
      parameters.add(readToken(token.get(), context.getName()));
    }
    return parameters;
  }

}

