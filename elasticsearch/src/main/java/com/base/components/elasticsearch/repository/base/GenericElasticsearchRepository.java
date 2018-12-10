/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.elasticsearch.repository.base;

import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.List;

/**
 * Elasticsearch Repository 整合JPA基础接口，其它Repository都基础此接口
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2017-11-28 12:54
 */
@NoRepositoryBean
public interface GenericElasticsearchRepository<T, ID extends Serializable>
  extends Serializable, ElasticsearchRepository<T, ID> {


  /**
   * 全文索引 - 分页
   * @param query         - Nullable -  查询关键字，特别注意为空或空字符串时，会查询全部
   * @param split         - Nullable -  查询关键字的分割符，如：空格或+
   * @param pageable      - Nullable -  分页对象
   *
   * @return Page
   */
  Page<T> fullTextSearch(String query, String split, Pageable pageable);

  /**
   * 全文索引 - 分页
   * <pre>
   *   参数 xxxQuery 示例：
   *   模糊匹配某字段，分词后用OR分段匹配：       QueryBuilders.matchQuery
   *   模糊匹配某字段，分词后用AND分段匹配：      QueryBuilders.matchPhraseQuery
   *   精确匹配某字段：                          QueryBuilders.termQuery 或 QueryBuilders.termsQuery
   * </pre>
   *
   * @param query         - Nullable -  查询关键字，特别注意为空（blank）时，会查询全部
   * @param split         - Nullable -  查询关键字的分割符，如：空格或+
   * @param pageable      - Nullable -  分页对象
   * @param shouldQuery   - Nullable -  可能匹配的其它查询，查询关键字为空时无效
   * @param mustQuery     - Nullable -  必须匹配的其它查询
   * @param mustNotQuery  - Nullable -  一定不匹配的其它查询
   *
   * @return Page
   */
  Page<T> fullTextSearch(String query, String split, Pageable pageable, List<QueryBuilder> shouldQuery,
                         List<QueryBuilder> mustQuery, List<QueryBuilder> mustNotQuery);

  /**
   * 全文索引 - 列表
   * @param query         - Nullable -  查询关键字，特别注意为空（blank）时，会返回全部对象集合！
   * @param split         - Nullable -  查询关键字的分割符，如：空格或+
   * @param sort          - Nullable -  排序对象
   *
   * @return List
   */
  List<T> fullTextSearch(String query, String split, Sort sort);

  /**
   * 全文索引 - 列表
   * <pre>
   *   参数 xxxQuery 示例：
   *   模糊匹配某字段，分词后用OR分段匹配：       QueryBuilders.matchQuery
   *   模糊匹配某字段，分词后用AND分段匹配：      QueryBuilders.matchPhraseQuery
   *   精确匹配某字段：                          QueryBuilders.termQuery 或 QueryBuilders.termsQuery
   * </pre>
   *
   * @param query         - Nullable -  查询关键字，特别注意为空（blank）时，会返回全部对象集合！
   * @param split         - Nullable -  查询关键字的分割符，如：空格或+
   * @param sort          - Nullable -  排序对象
   * @param shouldQuery   - Nullable -  可能匹配的其它查询，查询关键字为空时无效
   * @param mustQuery     - Nullable -  必须匹配的其它查询
   * @param mustNotQuery  - Nullable -  一定不匹配的其它查询
   * @return List
   */
  List<T> fullTextSearch(String query, String split, Sort sort, List<QueryBuilder> shouldQuery,
                         List<QueryBuilder> mustQuery, List<QueryBuilder> mustNotQuery);

  /**
   * 全文索引 - ID列表
   * @param query         - Nullable -  查询关键字
   * @param split         - Nullable -  查询关键字的分割符，如：空格或+
   * @param pageable      - Nullable -  分页对象
   *
   * @return List
   */
  List<ID> searchIds(String query, String split, Pageable pageable);

  /**
   * 条件查询分页
   * @param criteria      - Nonnull  -  动态查询条件
   * @param pageable      - Nullable -  分页对象
   *
   * @return Page
   */
  Page<T> criteriaSearch(Criteria criteria, Pageable pageable);

  /**
   * 条件查询分页
   * @param criteria      - Nonnull  -  动态查询条件
   * @param pageable      - Nullable -  分页对象
   * @param resultFields  - Nullable -  返回值字段名，非空时，返回值对象中只有这些字段有值
   *
   * @return Page
   */
  Page<T> criteriaSearch(Criteria criteria, Pageable pageable, String... resultFields);

  /**
   * 重构索引 - 删除全部索引并以数据库为准重构，hard 模式，慎用
   * @return 更新数
   */
  int rebuildIndexWithAll();

  /**
   * 重构索引 - 以数据库为准更新重构，soft 模式
   * @return 更新数
   */
  int rebuildIndexWithIncrement();


  /**
   * 重构索引 - 去除索引中存在，但数据库中不存在的索引，mixed 模式
   * @return 更新数
   */
  int rebuildIndexWithNotPersistent();


  /**
   * 获取ID字段名称
   */
  String getIdAttribute();

  /**
   * 对已有criteria对象，增加动态 AND 条件
   * @param criteria 非空的criteria对象
   * @param field 增加条件的字段
   * @param val 字段值
   * @param opKey 连接符
   */
  Criteria andCriteria(Criteria criteria, String field, Object val, Criteria.OperationKey opKey);
}
