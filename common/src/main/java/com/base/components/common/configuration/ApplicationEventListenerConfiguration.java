package com.base.components.common.configuration;

import com.base.components.common.util.JsonUtils;
import com.base.components.common.util.Logs;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ApplicationEventListenerConfiguration
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-02-05 10:00
 */
@Configuration
@Order
public class ApplicationEventListenerConfiguration {

  @Autowired(required = false)
  private AbstractJackson2HttpMessageConverter converter;

  /**
   * 启动完成事件
   * <p>
   *   顺序：WebServerInitializedEvent > ApplicationStartedEvent > ApplicationReadyEvent
   * </p>
   *//*
    @Bean
    public ApplicationListener<ApplicationReadyEvent> applicationReadyEventListener() {
      return new ApplicationListener<ApplicationReadyEvent>() {
        @Override
        public void onApplicationEvent(ApplicationReadyEvent event) {
        }
      };
    }*/

  /**
   * 注册自定义的ObjectNode格式化
   */
  @PostConstruct
  public void init() {
    if(converter != null){
      ObjectMapper mapper = converter.getObjectMapper();
      SimpleModule module = new SimpleModule();
      module.addSerializer(new JsonSerializer<ObjectNode>() {
        @Override
        public void serialize(ObjectNode value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
          Map v = JsonUtils.convert(value, new TypeReference<LinkedHashMap>() {
          });
          gen.writeObject(v);
        }
        @Override
        public Class<ObjectNode> handledType() {
          return ObjectNode.class;
        }
      });
      mapper.registerModule(module);
      Logs.get().info("register customizeJackson2ForObjectNode");
    }
  }
}
