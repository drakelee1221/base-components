package com.base.components.common.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.POJONode;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.util.Collection;

/**
 * @author <a href="drakelee1221@gmail.com">ligeng</a>
 * @version 1.0.0, 2017-07-03
 */
public class JsonUtils {
  public static final ObjectMapper mapper = new MoreAwareObjectMapper();

  public static ObjectNode createObjectNode() {
    return mapper.createObjectNode();
  }

  public static ArrayNode createArrayNode() {
    return mapper.createArrayNode();
  }

  public static String toString(Object value) throws IOException {
    return mapper.writeValueAsString(value);
  }

  public static void write(java.io.Writer w, Object value) throws IOException {
    mapper.writeValue(w, value);
  }

  public static void write(java.io.OutputStream out, Object value) throws IOException {
    mapper.writeValue(out, value);
  }

  /**
   * JSON字符串转对象
   *
   * @param content json字符串
   * @param valueType 目标对象类型
   * @param <T>
   *
   * @return
   *
   * @throws IOException
   */
  public static <T> T reader(String content, Class<T> valueType) throws IOException {
    return mapper.readValue(content, valueType);
  }

  /**
   * JSON字符串转对象
   *
   * @param src json字符串
   * @param valueTypeRef 目标对象类型
   * @param <T>
   *
   * @return
   *
   * @throws IOException
   */
  public static <T> T reader(String src, TypeReference<T> valueTypeRef) throws IOException {
    return mapper.readValue(src, valueTypeRef);
  }

  /**
   * 转换实体对象
   *
   * @param fromValue 实体对象，非JSON字符串
   * @param valueType 目标对象类型
   * @param <T>
   *
   * @return
   */
  public static <T> T convert(Object fromValue, Class<T> valueType) {
    return mapper.convertValue(fromValue, valueType);
  }

  /**
   * 转换实体对象
   *
   * @param fromValue 实体对象，非JSON字符串
   * @param valueTypeRef 目标对象类型
   * @param <T>
   *
   * @return
   */
  public static <T> T convert(Object fromValue, TypeReference<T> valueTypeRef) {
    return mapper.convertValue(fromValue, valueTypeRef);
  }


  /**
   * 从源对象中，获取后转换为 ObjectNode 对象，无论该对象存不存在，都会将此对象重新 SET 到源对象中进去
   * @param srcNode - 源 ObjectNode 对象
   * @param key     - 获取对象的 key 值
   * @return 获取的 ObjectNode 对象，获取到空对象时，会创建一个新对象
   */
  @NonNull
  public static ObjectNode findAndPutObjectNode(@NonNull ObjectNode srcNode, String key) {
    ObjectNode node = findObjectNode(srcNode, key);
    if (node == null) {
      node = srcNode.putObject(key);
    }
    return node;
  }

  /**
   * 从源对象中，获取后转换为 ObjectNode 对象，如果该对象存在，则会将此对象重新 SET 到源对象中进去
   * @param srcNode - 源 ObjectNode 对象
   * @param key     - 获取对象的 key 值
   * @return 获取的 ObjectNode 对象，可能为空
   */
  @Nullable
  public static ObjectNode findObjectNode(@NonNull ObjectNode srcNode, String key) {
    ObjectNode node = toObjectNode(srcNode.get(key));
    if(node != null){
      srcNode.set(key, node);
    }
    return node;
  }


  /**
   * 从源数组中，获取后转换为 ObjectNode 对象，无论该对象存不存在，都会将此对象重新 SET 到源数组中进去
   * @param srcNode - 源 ArrayNode 数组对象
   * @param index   - 获取数组的 index
   * @return 获取的 ObjectNode 对象，获取到空对象时，会创建一个新对象
   */
  @NonNull
  public static ObjectNode findAndPutObjectNode(@NonNull ArrayNode srcNode, int index) {
    ObjectNode node = findObjectNode(srcNode, index);
    if (node == null) {
      node = createObjectNode();
    }
    srcNode.set(index, node);
    return node;
  }

  /**
   * 从源数组中，获取后转换为 ObjectNode 对象，如果该对象存在，则会将此对象重新 SET 到源数组中进去
   * @param srcNode - 源 ArrayNode 数组对象
   * @param index   - 获取数组的 index
   * @return 获取的 ObjectNode 对象，可能为空
   */
  @Nullable
  public static ObjectNode findObjectNode(@NonNull ArrayNode srcNode, int index) {
    ObjectNode node = toObjectNode(srcNode.get(index));
    if(node != null){
      srcNode.set(index, node);
    }
    return node;
  }

  /**
   * 转换为 ObjectNode 对象
   * @param node -
   *
   * @return ObjectNode
   */
  public static ObjectNode toObjectNode(JsonNode node) {
    if (node != null) {
      if (node instanceof ObjectNode) {
        return (ObjectNode) node;
      }
      else if (node.isArray() || (node.isPojo() && ((POJONode)node).getPojo() instanceof Collection)) {
        throw new IllegalArgumentException("JsonNode is an array node !");
      }
      else {
        return convert(node, ObjectNode.class);
      }
    }
    return null;
  }

}
