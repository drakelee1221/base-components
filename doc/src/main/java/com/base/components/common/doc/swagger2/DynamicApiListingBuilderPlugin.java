package com.base.components.common.doc.swagger2;

import com.base.components.common.doc.annotation.Param;
import com.base.components.common.doc.annotation.RequestBodyModel;
import com.base.components.common.doc.annotation.ReturnModel;
import com.base.components.common.doc.type.ArrayType;
import com.base.components.common.doc.type.DataType;
import com.base.components.common.doc.type.ObjectType;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import springfox.documentation.builders.ModelBuilder;
import springfox.documentation.builders.ModelPropertyBuilder;
import springfox.documentation.schema.Model;
import springfox.documentation.schema.ModelProperty;
import springfox.documentation.schema.Types;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.ApiListingBuilderPlugin;
import springfox.documentation.spi.service.contexts.ApiListingContext;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import static com.base.components.common.doc.DocConstants.*;

/**
 * DynamicApiListingBuilderPlugin - 构建API动态对象
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-08-21 14:14
 */
public class DynamicApiListingBuilderPlugin implements ApiListingBuilderPlugin {
  @Autowired
  private TypeResolver typeResolver;

  @Override
  public void apply(ApiListingContext apiListingContext) {
    Class<?> controller = apiListingContext.getResourceGroup().getControllerClass().orNull();
    if (controller != null) {
      Map<String, Model> modelMap = Maps.newHashMap();
      modelMap.putAll(readReturnModels(controller));
      modelMap.putAll(readRequestBodyModels(controller));
      apiListingContext.apiListingBuilder().models(modelMap);
    }
  }

  private Map<String, Model> readRequestBodyModels(Class<?> controller) {
    Map<String, Model> modelMap = Maps.newHashMap();
    Map<String, RequestBodyModel> rsMap = Maps.newHashMap();
    String name = RequestBodyModel.class.getSimpleName();
    for (Method method : controller.getDeclaredMethods()) {
      RequestBodyModel requestBodyModel = AnnotationUtils.findAnnotation(method, RequestBodyModel.class);
      if (requestBodyModel != null) {
        rsMap.put(name + DYNAMIC_BEAN_SPLIT + requestBodyModel.hashCode(), requestBodyModel);
      }
    }
    for (Map.Entry<String, RequestBodyModel> entry : rsMap.entrySet()) {
      RequestBodyModel requestBodyModel = entry.getValue();
      Map<String, ModelProperty> propertyMap = Maps.newLinkedHashMap();
      for (Param property : requestBodyModel.value()) {
        Assert.hasText(property.name(), controller + " > Param name is null !");
        ModelProperty p = buildProperty(property, property.name(), entry.getKey(), modelMap);
        propertyMap.put(p.getName(), p);
      }
      Model model = buildModel(
        entry.getKey(), name, requestBodyModel.description(), propertyMap, typeResolver.resolve(Object.class),
        modelMap
      );
      modelMap.put(entry.getKey(), model);
    }
    return modelMap;
  }

  private Map<String, Model> readReturnModels(Class<?> controller) {
    Map<String, Model> modelMap = Maps.newHashMap();
    Map<String, ReturnModel> rsMap = Maps.newHashMap();
    String name = ReturnModel.class.getSimpleName();
    for (Method method : controller.getDeclaredMethods()) {
      ReturnModel returnModel = AnnotationUtils.findAnnotation(method, ReturnModel.class);
      if (returnModel != null) {
        rsMap.put(name + DYNAMIC_BEAN_SPLIT + returnModel.hashCode(), returnModel);
      }
    }
    for (Map.Entry<String, ReturnModel> entry : rsMap.entrySet()) {
      ReturnModel returnModel = entry.getValue();
      Map<String, ModelProperty> propertyMap = Maps.newLinkedHashMap();
      for (Param property : returnModel.value()) {
        Assert.hasText(property.name(), controller + " > Param name is null !");
        ModelProperty p = buildProperty(property, property.name(), entry.getKey(), modelMap);
        propertyMap.put(p.getName(), p);
      }
      Class<?> baseModel = returnModel.baseModel();
      if (!Void.class.equals(baseModel)) {
        Model model = ModelReferenceProvider.build(typeResolver, modelMap).newModel(baseModel);
        Map<String, ModelProperty> baseProperties = model.getProperties();
        baseProperties.putAll(propertyMap);
        propertyMap = baseProperties;
      }
      Model model = buildModel(entry.getKey(), name, null, propertyMap, typeResolver.resolve(Object.class), modelMap);
      modelMap.put(entry.getKey(), model);
    }
    return modelMap;
  }

  private Model buildModel(String id, String name, String desc, Map<String, ModelProperty> propertyMap,
                           ResolvedType type, Map<String, Model> modelMap) {
    Model model = modelMap.get(id);
    if (model == null) {
      model = new ModelBuilder().id(id).name(name).type(type).properties(propertyMap).description(desc).build();
      modelMap.put(id, model);
    } else {
      model.getProperties().putAll(propertyMap);
    }
    return model;
  }

  private ModelProperty buildProperty(Param property, String name, String modelId, Map<String, Model> modelMap) {
    ResolvedType type = typeResolver.resolve(property.dataType());
    String refId = null;
    String desc = property.value();
    Object example = null;
    boolean isCollection = false;
    String clearName = clearName(name);

    if (ArrayType.class.equals(property.dataType())) {
      type = typeResolver.resolve(Collection.class, property.genericType());
      refId = buildId(modelId, clearName);
      Model model = buildModel(refId, clearName, desc, Maps.newLinkedHashMap(), type, modelMap);
      isCollection = true;
      if (Types.typeNameFor(property.genericType()) == null && !DataType.class
        .isAssignableFrom(property.genericType())) {
        Model typeModel = ModelReferenceProvider.build(typeResolver, modelMap).newModel(property.genericType());
        modelMap.put(typeModel.getId(), typeModel);
        model.getProperties().putAll(typeModel.getProperties());
      }
      example = ARRAY_EXAMPLE;
    } else if (Types.typeNameFor(property.dataType()) == null) {
      type = typeResolver.resolve(Object.class);
      refId = buildId(modelId, clearName);
      Model model = buildModel(refId, clearName, desc, Maps.newLinkedHashMap(), type, modelMap);
      Model typeModel = ModelReferenceProvider.build(typeResolver, modelMap).newModel(property.dataType());
      if(!ObjectType.class.equals(typeModel.getType().getErasedType())){
        modelMap.put(typeModel.getId(), typeModel);
      }
      model.getProperties().putAll(typeModel.getProperties());
      example = OBJECT_EXAMPLE;
    }

    if (name.contains(ARRAY_SPLIT + OBJECT_SPLIT)) {
      int i = name.indexOf(ARRAY_SPLIT + OBJECT_SPLIT);
      String[] arr = {name.substring(0, i), name.substring(i + (ARRAY_SPLIT + OBJECT_SPLIT).length())};
      String propertyName = arr[0];
      refId = buildId(modelId, propertyName);
      Model exist = modelMap.get(refId);
      if (exist == null) {
        exist = buildModel(refId, propertyName, null, Maps.newLinkedHashMap(), typeResolver.resolve(Collection.class),
                           modelMap
        );
      }
      ModelProperty modelProperty = buildProperty(property, arr[1], exist.getId(), modelMap);
      exist.getProperties().put(clearName(arr[1]), modelProperty);
      desc = exist.getDescription();
      isCollection = true;
    } else if (name.contains(OBJECT_SPLIT)) {
      int i = name.indexOf(OBJECT_SPLIT);
      String[] arr = {name.substring(0, i), name.substring(i + OBJECT_SPLIT.length())};
      String propertyName = arr[0];
      refId = buildId(modelId, propertyName);
      Model exist = modelMap.get(refId);
      if (exist == null) {
        exist = buildModel(refId, propertyName, null, Maps.newLinkedHashMap(), typeResolver.resolve(Object.class),
                           modelMap
        );
      }
      ModelProperty modelProperty = buildProperty(property, arr[1], exist.getId(), modelMap);
      exist.getProperties().put(clearName(arr[1]), modelProperty);
      desc = exist.getDescription();
    }
    else if (example == null) {
      example = ExampleHelper.parseExample(type.getErasedType(), property.example());
    }
    ModelProperty p = new ModelPropertyBuilder().name(clearName(name)).required(property.required()).description(desc)
                                                .isHidden(property.hidden()).type(type).example(example).build();
    p.updateModelRef(ModelReferenceProvider.build(typeResolver, modelMap).ref(refId, isCollection));
    return p;
  }

  private String clearName(String src) {
    String name = src;
    if (name.contains(ARRAY_SPLIT)) {
      name = name.substring(0, name.indexOf(ARRAY_SPLIT));
    }
    if (name.contains(OBJECT_SPLIT)) {
      name = name.substring(0, name.indexOf(OBJECT_SPLIT));
    }
    return name;
  }

  private String buildId(String modelId, String name) {
    return modelId + BEAN_NAME_SPLIT + name;
  }


  @Override
  public boolean supports(DocumentationType delimiter) {
    return true;
  }
}
