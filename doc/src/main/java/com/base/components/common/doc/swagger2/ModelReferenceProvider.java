/*
 *
 *  Copyright 2015-2018 the original author or authors.
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
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.builders.ModelBuilder;
import springfox.documentation.builders.ModelPropertyBuilder;
import springfox.documentation.schema.Model;
import springfox.documentation.schema.ModelProperty;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.schema.ModelReference;
import springfox.documentation.schema.Types;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Map;

import static springfox.documentation.schema.Collections.collectionElementType;
import static springfox.documentation.schema.Collections.isContainerType;
import static springfox.documentation.schema.Maps.isMapType;
import static springfox.documentation.schema.Maps.mapValueType;
import static springfox.documentation.schema.ResolvedTypes.allowableValues;

/**
 * ModelReferenceProvider - 引用对象构造器
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-08-20 10:52
 */
public class ModelReferenceProvider implements Function<ResolvedType, ModelReference> {
  private final TypeResolver typeResolver;
  private final Map<String, Model> modelMap;

  private ModelReferenceProvider(TypeResolver typeResolver, Map<String, Model> modelMap) {
    this.typeResolver = typeResolver;
    this.modelMap = modelMap;
  }
  public static ModelReferenceProvider build(TypeResolver typeResolver, Map<String, Model> modelMap){
    return new ModelReferenceProvider(typeResolver, modelMap);
  }

  @Override
  public ModelReference apply(ResolvedType type) {
    return collectionReference(type).or(mapReference(type)).or(modelReference(type));
  }

  private ModelReference modelReference(ResolvedType type) {
    if (Void.class.equals(type.getErasedType()) || Void.TYPE.equals(type.getErasedType())) {
      return new ModelRef("void");
    }
    if (MultipartFile.class.isAssignableFrom(type.getErasedType())) {
      return new ModelRef("__file");
    }
    String typeName = typeName(type);
    return new ModelRef(typeName, allowableValues(type));
  }

  private Optional<ModelReference> mapReference(ResolvedType type) {
    if (isMapType(type)) {
      ResolvedType mapValueType = mapValueType(type);
      String typeName = typeName(type);
      return Optional.<ModelReference>of(new ModelRef(typeName, apply(mapValueType), true));
    }
    return Optional.absent();
  }

  private Optional<ModelReference> collectionReference(ResolvedType type) {
    if (isContainerType(type)) {
      ResolvedType collectionElementType = collectionElementType(type);
      String typeName = typeName(type);
      return Optional.<ModelReference>of(
        new ModelRef(typeName, apply(collectionElementType), allowableValues(collectionElementType)));
    }
    return Optional.absent();
  }

  private String typeName(ResolvedType input) {
    ResolvedType type = input == null ? dataType(String.class) : input;
    String typeName = Types.typeNameFor(type.getErasedType());
    if (StringUtils.isBlank(typeName)) {
      typeName = type.getErasedType().getName();
      if (!modelMap.containsKey(typeName) && !isContainerType(type) && !isMapType(type)) {
        try {
          modelMap.put(typeName, newModel(Class.forName(typeName)));
        } catch (Exception ignore) {
        }
      }
    }
    return typeName;
  }

  public Model newModel(Class<?> modelClass) {
    Model model = modelMap.get(modelClass.getName());
    Map<String, ModelProperty> propertyMap = Maps.newLinkedHashMap();
    if (model == null && Types.typeNameFor(modelClass) == null) {
      for (Field field : modelClass.getDeclaredFields()) {
        if (!Modifier.isStatic(field.getModifiers())) {
          Param property = field.getAnnotation(
            Param.class);
          String desc = null;
          boolean require = false;
          boolean hide = false;
          Object example = null;
          if (property != null) {
            desc = property.value();
            require = property.required();
            hide = property.hidden();
            example = ExampleHelper.parseExample(field.getType(), property.example());
          }
          ModelProperty p = new ModelPropertyBuilder().name(field.getName()).description(desc).required(require)
                                                      .isHidden(hide).example(example)
                                                      .type(dataType(field.getGenericType())).build();
          p.updateModelRef(ref(null, false));
          propertyMap.put(field.getName(), p);

        }
      }
      model = new ModelBuilder().id(modelClass.getName()).name(modelClass.getSimpleName())
                                .type(dataType(modelClass)).properties(propertyMap).build();
    } else {
      model = new ModelBuilder().id(modelClass.getName()).name(modelClass.getSimpleName())
                                .type(dataType(Object.class)).build();
    }
    return model;
  }

  public Function<ResolvedType, ? extends ModelReference> ref(String refId, boolean isCollection) {
    if (StringUtils.isNotBlank(refId)) {
      return (input) -> {
        if (isCollection || ArrayType.class.equals(input.getErasedType())) {
          return new ModelRef(refId, new ModelRef(refId));
        }
        return new ModelRef(refId);
      };
    } else {
      return (input) -> apply(input);
    }
  }


  private ResolvedType dataType(Type dataType) {
    return typeResolver.resolve(dataType);
  }
}
