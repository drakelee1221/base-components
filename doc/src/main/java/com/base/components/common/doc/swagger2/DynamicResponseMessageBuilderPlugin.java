package com.base.components.common.doc.swagger2;

import com.fasterxml.classmate.TypeResolver;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.base.components.common.doc.annotation.ReturnModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.schema.Collections;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ResponseMessage;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.contexts.OperationContext;

import java.util.Collection;
import java.util.Map;

import static com.base.components.common.doc.DocConstants.DYNAMIC_BEAN_SPLIT;

/**
 * DynamicResponseMessageBuilderPlugin - 构建动态返回对象引用
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-08-21 14:08
 */
public class DynamicResponseMessageBuilderPlugin implements OperationBuilderPlugin {
  @Autowired
  private TypeResolver typeResolver;

  @Override
  public boolean supports(DocumentationType delimiter) {
    return true;
  }

  @Override
  public void apply(OperationContext context) {
    Optional<ReturnModel> annotation = context.findAnnotation(ReturnModel.class);
    if (annotation.isPresent()) {
      ReturnModel returnModel = annotation.get();
      RequestMapping requestMapping = context.findAnnotation(RequestMapping.class).get();
      int code = HttpStatus.OK.value();
      Map<Integer, ResponseMessage> map = Maps.newHashMap();
      map.put(code, new ResponseMessageBuilder().code(code).responseModel(new ModelRef("void")).build());
      if (requestMapping != null) {
        RequestMethod[] methods = requestMapping.method();
        if (methods != null && methods.length > 0) {
          if (!RequestMethod.GET.equals(methods[0])) {
            code = HttpStatus.CREATED.value();
          }
        }
      }
      if (returnModel.isCollection()) {
        map.put(code, new ResponseMessageBuilder().code(code).responseModel(new ModelRef(
          Collections.containerType(typeResolver.resolve(Collection.class)),
          new ModelRef(ReturnModel.class.getSimpleName() + DYNAMIC_BEAN_SPLIT + returnModel.hashCode())
        )).build());
      } else {
        map.put(
          code, new ResponseMessageBuilder().code(code).responseModel(
            new ModelRef(ReturnModel.class.getSimpleName() + DYNAMIC_BEAN_SPLIT + returnModel.hashCode())).build());
      }

      context.operationBuilder().responseMessages(Sets.newHashSet(map.values()));
    }
  }
}
