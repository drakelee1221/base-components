package com.base.components.cache;

import com.base.components.common.boot.SpringBootApplicationRunner;
import com.base.components.common.tools.ClassFinder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

/**
 * Nameable - 使用枚举类实现此接口,
 * 实现的枚举类应在包路径下： SpringBootApplicationRunner.getProjectPackagePrefix() + ".cache"
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-09-10 9:27
 */
public interface Nameable {
  /**
   * 默认缓存名称，保存的永久不失效
   */
  Nameable DEFAULT_CACHE_NAME = DefaultCacheName.DEFAULT_CACHE;

  String PREFIX_DELIMITER = "::";

  String name();

  default String getPrefixCacheKey(String cacheKey){
    return getPrefix() + cacheKey;
  }


  /**
   * 构造统一缓存Key的前缀
   *
   * @return -
   */
  default String getPrefix(){
    return this.name() + PREFIX_DELIMITER;
  }

  /**
   * 构造统一缓存Key
   *
   * @param cacheName 缓存类别
   * @param cacheKey -
   *
   * @return -
   */
  static String getPrefixCacheKey(String cacheName, String cacheKey) {
    return getPrefix(cacheName) + cacheKey;
  }

  /**
   * 构造统一缓存Key的前缀
   *
   * @param cacheName 缓存类别
   *
   * @return -
   */
  static String getPrefix(String cacheName) {
    return cacheName + PREFIX_DELIMITER;
  }

  static Set<Nameable> getAll(){
    return AllNames.ALL_NAMES;
  }

  static Set<String> getAllNames(){
    return AllNames.ALL_NAME_STRINGS;
  }

  @SuppressWarnings("unchecked")
  static <N extends Nameable> N parse(String cacheName) {
    if (StringUtils.isNotBlank(cacheName)) {
      for (Nameable name : AllNames.ALL_NAMES) {
        if (cacheName.equalsIgnoreCase(name.name())) {
          return (N) name;
        }
      }
    }
    return null;
  }

  class AllNames {
    private static Set<Nameable> ALL_NAMES;
    private static Set<String> ALL_NAME_STRINGS;
    static {
      Map<String, Nameable> nameMap = Maps.newLinkedHashMap();
      nameMap.put(DEFAULT_CACHE_NAME.name(), DEFAULT_CACHE_NAME);
      Class<Nameable> nameableClass = Nameable.class;
      Class<Enum> enumClass = Enum.class;
      String pkg = SpringBootApplicationRunner.getProjectPackagePrefix() + ".cache";
      String error = null;
      try {
        for (Class<?> c : ClassFinder.findWithPackage(pkg)) {
          if (c != nameableClass && nameableClass.isAssignableFrom(c) && enumClass.isAssignableFrom(c)) {
            Method method = c.getMethod("values");
            method.setAccessible(true);
            Nameable[] values = (Nameable[]) method.invoke(null);
            for (Nameable value : values) {
              Nameable exists = nameMap.get(value.name());
              if(exists != null){
                error = exists.name() + " is repeat ！("+exists.getClass().getName()+" AND "+value.getClass().getName()+")";
                break;
              }
              else{
                nameMap.put(value.name(), value);
              }
            }
          }
        }
      } catch (Exception ignore) {
      }
      Assert.isTrue(error == null, error);
      ALL_NAMES = ImmutableSet.copyOf(nameMap.values());
      ALL_NAME_STRINGS = ImmutableSet.copyOf(nameMap.keySet());
    }
  }

  enum DefaultCacheName implements Nameable{
    DEFAULT_CACHE;
  }
}
