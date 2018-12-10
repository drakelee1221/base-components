package com.base.components.common.service.database;

import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Map;

/**
 * DatabaseExecutor - 数据库查询接口
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-07-27 9:52
 */
public interface DatabaseExecutor {

  /**
   * 查询 单列 ID 字段集合
   * @param sql     -  sql
   * @param <ID>    -
   *
   * @return ID 集合
   */
  <ID> List<ID> queryIds(@NonNull String sql);

  /**
   * 查询 多列集合
   * @param sql             -  sql
   * @param page            -  当前第几页（从 1 开始）
   * @param pageSize        -  分页返回最多多少行（大于等于 0）
   *
   * @return 多列集合
   */
  List<Map<String, Object>> queryRows(@NonNull String sql, int page, int pageSize);
}
