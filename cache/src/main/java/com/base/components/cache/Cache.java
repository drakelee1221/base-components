
package com.base.components.cache;

import com.base.components.common.dto.page.DataPage;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * 统一缓存接口，当前接口已支持事务，请用{@link org.springframework.transaction.annotation.Transactional} 声明
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2017-11-09 14:23
 */
public interface Cache extends org.springframework.cache.Cache {

  /**
   * 获取默认失效时间，秒
   * @return -
   */
  long getDefaultExpiresSecond();

  /**
   * 获取 cache 名称
   *
   * @return cache 名称
   */
  Nameable getCacheName();

  /**
   * 获取 cache 名称
   *
   * @return cache 名称
   */
  @Override
  @NonNull
  String getName();

  /**
   * 获取底层本地缓存接口。
   *
   * @return 如: {@link RedisCacheWriter}
   */
  @Override
  @NonNull
  Object getNativeCache();

  /**
   * 从缓存中获取 key 对应的值包装接口
   *
   * @param key -
   *
   * @return key 对应的值包装接口
   */
  @NonNull
  @Override
  ValueWrapper get(@NonNull Object key);

  /**
   * 从缓存中获取 key 对应的指定类型的值
   *
   * @param key -
   * @param type 返回值类型
   *
   * @return key 对应的值
   */
  @Nullable
  @Override
  <T> T get(@NonNull Object key, @Nullable Class<T> type);

  /**
   * 从缓存中获取 key 对应的值，如果缓存没有命中，则添加缓存，
   * 此时可异步地从 valueLoader 中获取对应的值，
   * 注意：添加并未设置默认失效时间，该{@code  Callable#call}的返回值会一直存在于缓存中
   *
   * @param key -
   * @param valueLoader -
   *
   * @return key 对应的值
   */
  @Deprecated
  @Nullable
  @Override
  <T> T get(@NonNull Object key, @Nullable Callable<T> valueLoader);

  /**
   * 缓存 key-value，如果缓存中已经有对应的 key，则替换其 value
   *
   * @param key -
   * @param value -
   */
  @Override
  void put(@NonNull Object key, @Nullable Object value);

  /**
   * 缓存 key-value，如果缓存中已经有对应的 key，则返回已有的 value，不做替换，
   * 如果缓存中沒有对应的 key，则put新的值，返回包装值为空的ValueWrapper对象
   * <pre><code>
   * Object existingValue = cache.get(key);
   * if (existingValue == null) {
   *     cache.put(key, value);
   *     return null;
   * } else {
   *     return existingValue;
   * }
   * </code></pre>
   *
   * @param key -
   * @param value -
   *
   * @return key 对应的值包装接口
   */
  @NonNull
  @Override
  ValueWrapper putIfAbsent(@NonNull Object key, @Nullable Object value);

  /**
   * 缓存 key-value，如果缓存中已经有对应的 key，则返回已有的 value，不做替换，
   * 如果缓存中沒有对应的 key，则put新的值，返回null
   *
   * @param key -
   * @param value -
   *
   * @return key 对应的值
   */
  @Nullable
  <T> T putIfAbsentReturnValue(@NonNull Object key, @Nullable Object value);

  /**
   * 从缓存中移除对应的 key
   *
   * @param key -
   */
  @Override
  void evict(@NonNull Object key);

  /**
   * 清空缓存
   */
  @Override
  void clear();



  /**
   * 获取缓存中key-value的剩余失效时间
   * @param key -
   * @param timeUnit 时间单位
   * @return 剩余失效时间
   */
  Long getExpire(Object key, TimeUnit timeUnit);

  /**
   * 设置缓存中key-value的失效时间
   * @param key -
   * @param timeout 失效时间
   * @param timeUnit 时间单位
   * @return 是否成功
   */
  Boolean expire(Object key, long timeout, TimeUnit timeUnit);

  /**
   * 设置缓存中key-value在expireDate时间点失效 
   * @param key -
   * @param expireDate 失效时间点
   * @return 是否成功
   */
  Boolean expireAt(Object key, Date expireDate);


  /**
   * 获取当前命名下所有 key
   * @param pattern 匹配符，如：
   * <pre>{@code
   *   KEYS * 匹配数据库中所有 key 。
   *   KEYS h?llo 匹配 hello ， hallo 和 hxllo 等。
   *   KEYS h*llo 匹配 hllo 和 heeeeello 等。
   *   KEYS h[ae]llo 匹配 hello 和 hallo ，但不匹配 hillo 。
   * }</pre>
   *
   * @return key集合
   */
  Set<String> keys(String pattern);

  /**
   * 获取当前命名下所有 key，当key数量较多时，不推荐使用
   * @return key集合
   */
  @Deprecated
  Set<String> keys();

  /**
   * 获取keys的所有值
   * @param keys
   * @return
   */
  <T> List<T> mget(Collection keys);

  /**
   * 获取列表分页
   *
   * @param key 列表key
   * @param pageNum 当前页数，默认1
   * @param pageSize 每页行数，小于1时表示获取全部列表
   * @return -
   */
  <T> DataPage<T> listsOfPage(Object key, int pageNum, int pageSize);

  /**
   * 获取列表
   *
   * @param key 列表key
   * @param pageNum 当前页数，默认1
   * @param pageSize 每页行数，小于1时表示获取全部列表
   * @return -
   */
  <T> List<T> listsOfList(Object key, int pageNum, int pageSize);

  /**
   * 根据index获取列表元素对象
   *
   * @param key 列表key
   * @param index 列表下标，小于0时，从列表尾部开始
   * @return -
   */
  <T> T listsOfIndex(Object key, long index);

  /**
   * 获取列表长度
   *
   * @param key 列表key
   * @return -
   */
  long listsOfSize(Object key);

  /**
   * 根据index替换列表元素对象
   *
   * @param key 列表key
   * @param index 列表下标，小于0时，从列表尾部开始
   * @param refreshExpireTime 是否刷新key的默认过期时间（见cache.yml）
   */
  <T> void listsOfSet(Object key, long index, boolean refreshExpireTime, T value);

  /**
   * 向列表尾部添加值对象
   *
   * @param key 列表key
   * @param value 值对象
   * @param refreshExpireTime 是否刷新key的默认过期时间（见cache.yml）
   */
  <T> void listsOfAppend(Object key, boolean refreshExpireTime, T value);

  /**
   * 向列表尾部添加值对象
   *
   * @param key 列表key
   * @param values 值对象，请注意不要使用 {@link Collection#toArray()}，会将整个数组存储为一个对象
   * @param refreshExpireTime 是否刷新key的默认过期时间（见cache.yml）
   */
  <T> void listsOfAppend(Object key, boolean refreshExpireTime, T[] values);

  /**
   * 向列表头部添加值对象
   *
   * @param key 列表key
   * @param value 值对象
   * @param refreshExpireTime 是否刷新key的默认过期时间（见cache.yml）
   */
  <T> void listsOfPrepend(Object key, boolean refreshExpireTime, T value);

  /**
   * 向列表头部添加值对象
   *
   * @param key 列表key
   * @param values 值对象，请注意不要使用 {@link Collection#toArray()}，会将整个数组存储为一个对象
   * @param refreshExpireTime 是否刷新key的默认过期时间（见cache.yml）
   */
  <T> void listsOfPrepend(Object key, boolean refreshExpireTime, T[] values);

  /**
   * 删除列表元素
   *
   * @param key 列表key
   * @param count 删除发现相同value的顺序删除个数，负数取绝对值，0时删除全部相同的value
   * @param refreshExpireTime 是否刷新key的默认过期时间（见cache.yml）
   * @param value 值对象
   */
  <T> void listsOfRemove(Object key, long count, boolean refreshExpireTime, T value);



  /**
   * Set集合增加元素
   *
   * @param key Set集合key
   * @param refreshExpireTime 是否刷新key的默认过期时间（见cache.yml）
   * @param value 增加元素对象
   */
  <T> void setsOfAdd(Object key, boolean refreshExpireTime, T value);

  /**
   * Set集合增加元素
   *
   * @param key Set集合key
   * @param refreshExpireTime 是否刷新key的默认过期时间（见cache.yml）
   * @param values 增加元素对象，请注意不要使用 {@link Collection#toArray()}，会将整个数组存储为一个对象
   */
  <T> void setsOfAdd(Object key, boolean refreshExpireTime, T[] values);

  /**
   * Set集合删除元素
   *
   * @param key Set集合key
   * @param refreshExpireTime 是否刷新key的默认过期时间（见cache.yml）
   * @param value 增加元素对象
   */
  void setsOfRemove(Object key, boolean refreshExpireTime, Object value);

  /**
   * Set集合删除元素
   *
   * @param key Set集合key
   * @param refreshExpireTime 是否刷新key的默认过期时间（见cache.yml）
   * @param values 增加元素对象，请注意不要使用 {@link Collection#toArray()}，会将整个数组存储为一个对象
   */
  void setsOfRemove(Object key, boolean refreshExpireTime, Object[] values);

  /**
   * Set集合长度
   *
   * @param key Set集合key
   */
  long setsOfSize(Object key);

  /**
   * Set集合是否包含某个元素
   *
   * @param key Set集合key
   * @param value 元素对象
   * @return -
   */
  <T> boolean setsOfIsMember(Object key, T value);

  /**
   * Set集合获取全部元素
   *
   * @param key Set集合key
   * @return -
   */
  <T> Set<T> setsOfMembers(Object key);

  /**
   * Set集合获取 count 个元素
   *
   * @param key Set集合key
   * @param count 获取个数，小于1时返回空集合
   * @return -
   */
  <T> Set<T> setsOfMembers(Object key, long count);

  /**
   * Set集合获取 count 个随机元素，有可能返回多个重复的元素
   *
   * @param key Set集合key
   * @param count 获取个数，负数取绝对值，数量可以大于set的总长度
   * @return -
   */
  <T> List<T> setsOfRandomMembers(Object key, long count);

  /**
   * Set集合取差集，返回key集合存在的元素与在otherKeys集合中不存在的
   *
   * @param key Set集合key
   * @param otherKeys 其它Set集合key
   * @return -
   */
  <T> Set<T> setsOfDifference(Object key, Collection<T> otherKeys);

  /**
   * Set集合取交集，返回key集合存在的元素与在otherKeys集合中也存在的
   *
   * @param key Set集合key
   * @param otherKeys 其它Set集合key
   * @return -
   */
  <T> Set<T> setsOfIntersect(Object key, Collection<T> otherKeys);

  /**
   * Set集合取并集，返回key集合元素与在otherKeys集合的全部元素的总合去重
   *
   * @param key Set集合key
   * @param otherKeys 其它Set集合key
   * @return -
   */
  <T> Set<T> setsOfUnion(Object key, Collection<T> otherKeys);
}
