/*
 * Copyright 2015-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.base.components.cache.util;

import com.base.components.common.util.JsonUtils;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

/**
 * @author Mos.Jiang
 * @since 1.6
 */
public class GenericJackson2JsonRedisSerializerRewrite extends GenericJackson2JsonRedisSerializer {

  public GenericJackson2JsonRedisSerializerRewrite() {
    super(JsonTypeInfo.Id.CLASS.getDefaultPropertyName());
  }

  @Override
  public byte[] serialize(Object source) throws SerializationException {
    if (source instanceof ObjectNode) {
      ((ObjectNode) source).put(JsonTypeInfo.Id.CLASS.getDefaultPropertyName(), source.getClass().getName());
    } else if (source instanceof ArrayNode || source instanceof JsonNode) {
      ArrayNode temp = JsonUtils.createArrayNode();
      temp.add(source.getClass().getName());
      temp.addPOJO(source);
      source = temp;
    }
    return super.serialize(source);
  }
}