/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.common.util;

import com.base.components.common.constants.sys.Dates;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import javax.validation.constraints.NotNull;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 转换工具类
 *
 * @author <a href="drakelee1221@gmail.com">ligeng</a>
 * @version 1.0.0, 2017-07-22
 */
public class ConvertUtil {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final ConvertUtilsBean CONVERT = new ConvertUtilsBean();
  private static final BeanUtilsBean BEAN_UTILS = new BeanUtilsBean(CONVERT);

  static {
    CONVERT.register(false, true, 0);
    CONVERT.register(DateTimeConverter.getInstance(), Date.class);
    CONVERT.register(DateTimeConverter.getInstance(), java.sql.Date.class);
    CONVERT.register(DateTimeConverter.getInstance(), DateTime.class);
    CONVERT.register(DateTimeConverter.getInstance(), Timestamp.class);
  }

  /**
   * 类型转换
   *
   * @param src - Nullable -  需要转换的源对象
   * @param targetClass - Nullable -  转换目标类型，如：Integer、Double等
   *
   * @return 转换失败时返回 Null
   */
  @SuppressWarnings({"unchecked"})
  public static <T> T convert(Object src, Class<T> targetClass) {
    if (targetClass == null) {
      return null;
    }
    try {
      return (T) CONVERT.convert(src, targetClass);
    } catch (Exception e) {
      logger.error("", e);
    }
    return null;
  }

  /**
   * 类型转换
   *
   * @param src - Nullable -  需要转换的源对象
   * @param defaultValue - Nullable -  转换目标类型失败时的默认值
   *
   * @return 转换失败时返回 defaultValue
   */
  @SuppressWarnings({"unchecked"})
  public static <T> T convert(Object src, T defaultValue) {
    if (defaultValue == null) {
      return null;
    }
    try {
      Object afterValue = CONVERT.convert(src, defaultValue.getClass());
      if (afterValue != null) {
        return (T) afterValue;
      }
    } catch (Exception e) {
      logger.error("", e);
    }
    return defaultValue;
  }

  /**
   * 类型可空转换
   *
   * @param src - Nullable -  需要转换的源对象，toString时不能为空字符串
   * @param targetClass - Nullable -  转换目标类型，如：Integer、Double等
   *
   * @return 转换失败时返回 Null
   */
  @SuppressWarnings({"unchecked"})
  public static <T> T convertNullable(Object src, Class<T> targetClass) {
    if (src == null || StringUtils.isBlank(src.toString())) {
      return null;
    }
    try {
      return (T) CONVERT.convert(src, targetClass);
    } catch (Exception e) {
      logger.error("", e);
    }
    return null;
  }

  /**
   * 参数Map，非空转换
   *
   * @param paramMap - Nonnull  -  参数Map
   * @param key - Nonnull  -  参数Key
   * @param targetClass - Nonnull  -  转换目标类型，如：Integer、Double等
   *
   * @return targetClass 类型对象，参数为空或转换失败时抛出异常
   */
  public static <T> T checkNotNull(Map<String, ?> paramMap, String key, Class<T> targetClass) {
    return checkNotNull(paramMap, key, null, targetClass);
  }

  /**
   * 参数Map，非空转换
   *
   * @param paramMap - Nonnull  -  参数Map
   * @param key - Nonnull  -  参数Key
   * @param errorMessage - Nullable -  异常信息
   * @param targetClass - Nonnull  -  转换目标类型，如：Integer、Double等
   *
   * @return targetClass 类型对象，参数为空或转换失败时抛出异常
   */
  public static <T> T checkNotNull(Map<String, ?> paramMap, String key, String errorMessage, Class<T> targetClass) {
    Assert.notNull(paramMap, "paramMap is null");
    Assert.notNull(targetClass, "targetClass is null");
    Assert.notNull(key, "key is null");
    errorMessage = StringUtils.isEmpty(errorMessage) ? key + " must be not null" : errorMessage;
    Object val = paramMap.get(key);
    Assert.isTrue(null != val && StringUtils.isNotEmpty(val.toString()), errorMessage);
    T re = convert(val, targetClass);
    Assert.notNull(re, errorMessage);
    return re;
  }

  /**
   * 参数Map，可空转换成Double类型，参数Key对应值为null时，返回0
   *
   * @param paramMap - Nonnull  -  参数Map
   * @param key - Nonnull  -  参数Key
   * @param min - Nullable  - 参数Key对应值最小值（包含最小值），null无限制最小值
   * @param max - Nullable  - 参数Key对应值最大值（包含最大值），null无限制最大值
   *
   * @return Double 类型对象，参数为空或转换失败时抛出异常
   */
  public static Double convertDoubleNullable(Map<String, ?> paramMap, String key,Double min,Double max) {
    return convertDoubleNullable(paramMap,key,min,max,0D);
  }

  /**
   * 参数Map，可空转换成Double类型
   *
   * @param paramMap - Nonnull  -  参数Map
   * @param key - Nonnull  -  参数Key
   * @param min - Nullable  - 传参最小值，null无限制最小值
   * @param max - Nullable  -  传参最大值，null无限制最大值
   * @param defaultValue - Nonnull  -  当key所对应得value为null，返回得默认值
   *
   * @return Double 类型对象，参数为空或转换失败时抛出异常
   */
  public static Double convertDoubleNullable(Map<String, ?> paramMap, String key,Double min,Double max,Double defaultValue) {
    Assert.notNull(paramMap, "paramMap is null");
    Assert.notNull(key, "key is null");
    Assert.notNull(defaultValue, "defaultValue is null");
    Object val = paramMap.get(key);
    if (val == null) {
      return defaultValue;
    }
    Double re = convert(val, Double.class);
    if(min!=null && re<min){
      throw new IllegalArgumentException(re + " must be bigger than " + min);
    }
    if(max !=null && re>max){
      throw new IllegalArgumentException(re + " must be smaller than " + max);
    }
    return re;
  }

  /**
   * 给一个对象设置成员属性
   *
   * @param object 非空对象
   * @param properties 成员属性Map
   *
   * @return object
   */
  public static <T> T populate(T object, Map<String, ?> properties) {
    return populate(object, properties, true);
  }

  public static <T> T populate(T object, Map<String, ?> properties, boolean throwException) {
    Assert.notNull(object, "object is null");
    if (properties != null && !properties.isEmpty()) {
      for (Map.Entry<String, ?> entry : properties.entrySet()) {
        try {
          BEAN_UTILS.setProperty(object, entry.getKey(), entry.getValue());
        } catch (Exception e) {
          if (throwException) {
            throw new IllegalArgumentException(e);
          } else {
            logger.warn(e.getMessage());
          }
        }
      }
    }
    return object;
  }


  /**
   * 为实体对象设置参数
   */
  public static <T> void setPropertyIfNotBlank(T object, String key, Object value){
    Assert.notNull(object, "object is null");
    Assert.notNull(key, "key is null");
    if(value != null && StringUtils.isNotBlank(value.toString())){
      try {
        BEAN_UTILS.setProperty(object, key, value);
      } catch (Exception e) {
        throw new IllegalArgumentException(e);
      }
    }
  }

  /**
   * 为实体对象设置参数
   */
  public static <T> void setPropertyIfNotBlank(T object, String key, Map<String, ?> params){
    Assert.notNull(object, "object is null");
    Assert.notNull(key, "key is null");
    if(params != null){
      Object value = params.get(key);
      setPropertyIfNotBlank(object, key, value);
    }
  }


  /**
   * {@code
   * 将对象列表转换为以某字段分组的 Multimap，
   * 一般用于从表查询后的分组组装。
   *
   * 如：先查询主表得到 mainList，再调用 {@link #transformMap(Collection, Function) }
   * 得到 Map<ID, Main> mainMap, 将 mainMap 的 keySet 集合ID用于查询从表的外键字段，
   * 得到 relationList, 将此从表集合和一个能通过从表对象得到主表ID的匿名函数，
   * 传入本方法中即可得到分组 Multimap，最后遍历主表时通过主表ID即能得到从表对应的关联集合
   * }
   *
   * @param list - Nonnull  -  对象集合
   * @param applyFunction - Nonnull  -  得到分组字段的方法，返回值为空时抛出异常
   * @param <ITEM> - Nonnull  -  对象泛型
   * @param <KEY> - Nonnull  -  对象ID泛型
   *
   * @return 非空，{@code
   * Map <对象分组字段, 对象集合>
   * }
   */
  public static <ITEM, KEY> Multimap<KEY, ITEM> transformMultiMap(@NotNull Collection<ITEM> list,
                                                                  @NotNull Function<ITEM, KEY> applyFunction) {
    Multimap<KEY, ITEM> map = LinkedListMultimap.create();
    if (list != null && applyFunction != null) {
      for (ITEM item : list) {
        KEY key = applyFunction.apply(item);
        Assert.notNull(key, "key is null, item = " + item);
        map.put(key, item);
      }
    }
    return map;
  }

  /**
   * {@code
   * 将对象列表转换为以ID字段分组的Map
   * }
   * 将对象列表转换为以唯一字段分组的Map：Map &lt; 对象分组字段, 对象 &gt;
   *
   * @param list - Nonnull  -  对象集合
   * @param applyFunction - Nonnull  -  得到分组字段的方法，返回值为空时抛出异常
   * @param <ITEM> - Nonnull  -  对象泛型
   * @param <KEY> - Nonnull  -  对象ID泛型
   *
   * @return 非空，{@code
   * Map <对象ID, 单个对象>
   * }
   *
   * @throws IllegalArgumentException 当存在相同的ID字段时
   */
  @NotNull
  public static <ITEM, KEY> Map<KEY, ITEM> transformMap(@NotNull Collection<ITEM> list,
                                                        @NotNull Function<ITEM, KEY> applyFunction) {
    Map<KEY, ITEM> map = Maps.newLinkedHashMap();
    if (list != null && applyFunction != null) {
      for (ITEM item : list) {
        KEY key = applyFunction.apply(item);
        Assert.notNull(key, "key is null, item = " + item);
        ITEM exist = map.get(key);
        Assert.isNull(exist, "Multiple entries with same key: " + key + " value: " + item + " and " + exist);
        map.put(key, item);
      }
    }
    return map;
  }


  /**
   * 日期转换，源字符串为空时，返回空，其它情况抛出异常
   *
   * @param src - Nullable  -  源字符串
   * @param srcName - Nullable  -  源字符串字段描述, 用于异常抛出
   * @param formatter - Nullable  -  日期转换器，更多见{@link Dates}
   *
   * @return DateTime - Nullable
   */
  public static DateTime dateNullable(String src, String srcName, DateTimeFormatter formatter) {
    try {
      if (StringUtils.isBlank(src)) {
        return null;
      }
      return Dates.parse(src, formatter);
    } catch (Exception e) {
      srcName = srcName == null ? "" : srcName + ",";
      throw new IllegalArgumentException(srcName + " 格式错误！");
    }
  }

  /**
   * 日期转换，源字符串为空或转换异常时，抛出异常
   *
   * @param src - Nonnull   -  源字符串
   * @param srcName - Nullable  -  源字符串字段描述, 用于异常抛出
   * @param formatter - Nullable  -  日期转换器，更多见{@link Dates}
   *
   * @return DateTime - Nonnull
   */
  @NotNull
  public static DateTime dateNonnull(String src, String srcName, DateTimeFormatter formatter) {
    srcName = srcName == null ? "" : srcName + ",";
    try {
      if (StringUtils.isBlank(src)) {
        throw new IllegalArgumentException(srcName + " 不能为空！");
      }
      return Dates.parse(src, formatter);
    } catch (Exception e) {
      throw new IllegalArgumentException(srcName + " 格式错误！");
    }
  }

  public static Map<String, List<Object>> transformJsonMultiMap(ArrayNode formArray, String key) {
    Map<String, List<Object>> resObj = new HashMap<>();
    for (int x = 0; x < formArray.size(); x++) {
      String gName = formArray.get(x).get(key).asText();
      List<Object> objects = resObj.get(gName);
      if (objects == null) {
        objects = new ArrayList<>();
      }
      objects.add(formArray.get(x));
      resObj.put(gName, objects);
    }
    return resObj;
  }

  /**
   * 支持格式： yyyy-MM-dd HH:mm:ss，yyyy-MM-dd，yyyy-MM-dd HH:mm
   * 支持类型： java.util.Date，java.sql.Date，java.sql.Timestamp，org.joda.time.DateTime
   */
  @SuppressWarnings("unchecked")
  public static class DateTimeConverter implements Converter {
    private static final DateTimeConverter INSTANCE = new DateTimeConverter();

    private DateTimeConverter() {
    }

    @Override
    public <T> T convert(Class<T> type, Object value) {
      if (value != null) {
        if (type == Date.class) {
          DateTime v = parse(value.toString());
          return v == null ? null : (T) v.toDate();
        }
        if (type == DateTime.class) {
          DateTime v = parse(value.toString());
          return v == null ? null : (T) v;
        }
        if (type == java.sql.Date.class) {
          DateTime v = parse(value.toString());
          return v == null ? null : (T) new java.sql.Date(v.getMillis());
        }
        if (type == Timestamp.class) {
          DateTime v = parse(value.toString());
          return v == null ? null : (T) new Timestamp(v.getMillis());
        }
      }
      return null;
    }

    private DateTime parse(String str) {
      DateTime v = parse(str, Dates.DATE_TIME_FORMATTER);
      if (v == null) {
        v = parse(str, Dates.DATE_FORMATTER);
      }
      if (v == null) {
        v = parse(str, Dates.DATE_MINUTE_FORMATTER);
      }
      return v;
    }

    private DateTime parse(String str, DateTimeFormatter formatter) {
      try {
        return Dates.parse(str, formatter);
      } catch (Exception ignore) {
        return null;
      }
    }

    public static DateTimeConverter getInstance() {
      return INSTANCE;
    }
  }

  /**
   * 对象转Map
   *
   * @param obj
   *
   * @return
   */
  public static Map convertObjToMap(Object obj) {
    if (null == obj) {
      return null;
    }
    Map<String, Object> reMap = new HashMap<>();
    Field[] fields = obj.getClass().getDeclaredFields();
    try {
      Arrays.asList(fields).stream().forEach(x -> {
        try {
          Field f = obj.getClass().getDeclaredField(x.getName());
          f.setAccessible(true);
          if (!"serialVersionUID".equalsIgnoreCase(x.getName())) {
            reMap.put(x.getName(), f.get(obj));
          }
        } catch (NoSuchFieldException e) {
          e.printStackTrace();
        } catch (IllegalArgumentException e) {
          e.printStackTrace();
        } catch (IllegalAccessException e) {
          e.printStackTrace();
        }
      });
    } catch (SecurityException e) {
      e.printStackTrace();
      return null;
    }
    return reMap;
  }

}
