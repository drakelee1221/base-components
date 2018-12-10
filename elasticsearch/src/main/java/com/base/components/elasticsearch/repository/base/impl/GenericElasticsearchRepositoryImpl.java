/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.elasticsearch.repository.base.impl;

import com.base.components.common.constants.sys.Pages;
import com.base.components.common.dto.page.DataPage;
import com.base.components.common.elasticsearch.DocumentIndexBuildQuery;
import com.base.components.common.service.database.DatabaseExecutor;
import com.base.components.elasticsearch.repository.base.CriteriaBuilder;
import com.base.components.elasticsearch.repository.base.GenericElasticsearchRepository;
import com.google.common.collect.Lists;
import com.base.components.common.util.ConvertUtil;
import com.base.components.common.util.Logs;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ResultsExtractor;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.repository.support.AbstractElasticsearchRepository;
import org.springframework.data.elasticsearch.repository.support.ElasticsearchEntityInformation;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * ElasticsearchRepository 基础方法实现
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2017-11-28 12:54
 */
@NoRepositoryBean
public class GenericElasticsearchRepositoryImpl<T, ID extends Serializable>
  extends AbstractElasticsearchRepository<T, ID> implements GenericElasticsearchRepository<T, ID> {
  private static final long serialVersionUID = 5519696574868356341L;
  private final Logger logger = LoggerFactory.getLogger(getClass());

  private static int REBUILD_INDEX_PAGE_LIMIT = 1000;

  private static final AtomicReference<DatabaseExecutor> DATABASE_EXECUTOR = new AtomicReference<>();

  private ElasticsearchEntityInformation<T, ID> metadata;

  private Map<String, Method> domainSetMethods = new HashMap<>();

//  public GenericElasticsearchRepositoryImpl() {
//  }
//
//  public GenericElasticsearchRepositoryImpl(ElasticsearchOperations elasticsearchOperations) {
//    super(elasticsearchOperations);
//  }

  public GenericElasticsearchRepositoryImpl(ElasticsearchEntityInformation<T, ID> metadata,
                                            ElasticsearchOperations elasticsearchOperations) {
    super(metadata, elasticsearchOperations);
    this.metadata = metadata;
    for (Method method : metadata.getJavaType().getMethods()) {
      String name = method.getName();
      if(name.startsWith("set")){
        domainSetMethods.put(name.substring(3).toUpperCase(), method);
      }
    }
  }

  @Override
  public String getIdAttribute(){
    return this.metadata.getIdAttribute();
  }

  @Override
  protected String stringIdRepresentation(ID id) {
    return id == null ? null : id.toString();
  }

  @Override
  public Page<T> fullTextSearch(String query, String split, Pageable pageable) {
    return fullTextSearch(query, split, pageable, null, null, null);
  }

  @Override
  public Page<T> fullTextSearch(String query, String split, Pageable pageable, List<QueryBuilder> shouldQuery,
                                List<QueryBuilder> mustQuery, List<QueryBuilder> mustNotQuery) {
    QueryBuilder queryBuilder = splitQueryString(query, split, shouldQuery, mustQuery, mustNotQuery);
    NativeSearchQueryBuilder nqb = new NativeSearchQueryBuilder()
      .withPageable(pageable)
      .withQuery(queryBuilder);
    if(pageable.getSort() == null){
      nqb.withSort(SortBuilders.scoreSort().order(SortOrder.DESC));
    }
    return search(nqb.build());
  }

  @Override
  public List<T> fullTextSearch(String query, String split, Sort sort) {
    return fullTextSearch(query, split, sort, null, null, null);
  }

  @Override
  public List<T> fullTextSearch(String query, String split, Sort sort, List<QueryBuilder> shouldQuery,
                                List<QueryBuilder> mustQuery, List<QueryBuilder> mustNotQuery) {
    QueryBuilder queryBuilder = splitQueryString(query, split, shouldQuery, mustQuery, mustNotQuery);
    NativeSearchQueryBuilder nqb = new NativeSearchQueryBuilder().withQuery(queryBuilder);
    if(sort == null){
      nqb.withSort(SortBuilders.scoreSort().order(SortOrder.DESC));
    }else{
      for (Sort.Order next : sort) {
        nqb.withSort(SortBuilders.fieldSort(next.getProperty()).order(
          next.getDirection() == null || next.getDirection().equals(Sort.Direction.ASC)
          ? SortOrder.ASC : SortOrder.DESC));
      }
    }
    return Lists.newArrayList(search(nqb.build()));
  }

  @Override
  public List<ID> searchIds(String query, String split, Pageable pageable){
    QueryBuilder queryBuilder = splitQueryString(query, split, null, null, null);
    NativeSearchQueryBuilder q = new NativeSearchQueryBuilder().withQuery(queryBuilder);
    q.withPageable(pageable);
    q.withIndices(metadata.getIndexName());
    q.withTypes(metadata.getType());
    q.withFields(metadata.getIdAttribute());
    return elasticsearchOperations.query(q.build(), new ResultsExtractor<List<ID>>() {
      @Override
      public List<ID> extract(SearchResponse response) {
        List<ID> ids = new ArrayList<>();
        for (SearchHit hit : response.getHits()) {
          if (hit != null) {
            ids.add(ConvertUtil.convert(hit.getId(), metadata.getIdType()));
          }
        }
        return ids;
      }
    });
  }

  @Override
  public Page<T> criteriaSearch(Criteria criteria, Pageable pageable) {
    if(criteria == null){
      return DataPage.getEmptyPage();
    }
    CriteriaQuery criteriaQuery = new CriteriaQuery(criteria);
    criteriaQuery.setPageable(pageable);
    return elasticsearchOperations.queryForPage(criteriaQuery, metadata.getJavaType());
  }

  @Override
  public Page<T> criteriaSearch(Criteria criteria, Pageable pageable, String... resultFields) {
    if(criteria == null){
      return DataPage.getEmptyPage();
    }
    CriteriaQuery criteriaQuery = new CriteriaQuery(criteria);
    criteriaQuery.setPageable(pageable);
    if(resultFields != null && resultFields.length > 0){
      criteriaQuery.addFields(resultFields);
    }
    return elasticsearchOperations.queryForPage(criteriaQuery, metadata.getJavaType());
  }


  @Override
  @Transactional(rollbackFor = Exception.class)
  public int rebuildIndexWithAll(){
    deleteAll();
    int count = updateIndex();
    logger.info(metadata.getJavaType().getName() + " rebuildIndexWithAll > " + count);
    return count;
  }


  @Override
  @Transactional(rollbackFor = Exception.class)
  public int rebuildIndexWithIncrement(){
    int count = updateIndex();
    logger.info(metadata.getJavaType().getName() + " rebuildIndexWithIncrement > " + count);
    return count;
  }

  @Override
  @SuppressWarnings("unchecked")
  @Transactional(rollbackFor = Exception.class)
  public int rebuildIndexWithNotPersistent(){
    DocumentIndexBuildQuery an = metadata.getJavaType().getAnnotation(DocumentIndexBuildQuery.class);
    Assert.isTrue(an != null && StringUtils.isNotBlank(an.value()),
                  metadata.getJavaType()+ ", 构建索引的sql语句为空！");
    DatabaseExecutor databaseExecutor = getDatabaseExecutor();
    Client client = elasticsearchOperations.getClient();
    BulkRequestBuilder bulk = client.prepareBulk().setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
    //自定义 Pages 从1开始
    int page = 1;
    int count = 0;
    List<ID> ids;
    while (true){
      ids = searchIds(null, null,
                      Pages.Helper.pageable(page, REBUILD_INDEX_PAGE_LIMIT, null));
      if(ids != null && !ids.isEmpty()){
        String sql = buildNotPersistentSql(ids, an.value(), an.idColumn());
        List<ID> notExistIds = databaseExecutor.queryIds(sql);
        for (ID id : notExistIds) {
          bulk.add(client.prepareDelete(metadata.getIndexName(), metadata.getType(), id.toString()));
        }
        page++;
        count += notExistIds.size();
      }else{
        break;
      }
    }
    if(count > 0){
      bulk.execute().actionGet();
    }
    logger.info(metadata.getJavaType().getName() + " rebuildIndexWithNotPersistent > " + count);
    return count;
  }

  @Override
  public Criteria andCriteria(Criteria criteria, String field, Object val, Criteria.OperationKey opKey) {
    return CriteriaBuilder.andCriteria(criteria, field, val, opKey);
  }

  @SuppressWarnings("unchecked")
  private int updateIndex(){
    DocumentIndexBuildQuery an = metadata.getJavaType().getAnnotation(DocumentIndexBuildQuery.class);
    Assert.isTrue(an != null && StringUtils.isNotBlank(an.value()),
                  metadata.getJavaType()+ ", 构建索引的sql语句为空！");
    DatabaseExecutor databaseExecutor = getDatabaseExecutor();
    int page = 1;
    int count = 0;
    List<T> list;
    while (true){
      List<Map<String, Object>> rows = databaseExecutor.queryRows(an.value(),
                                                                  page,
                                                                  REBUILD_INDEX_PAGE_LIMIT);
      list = transformer(rows, an.columnPrefix(), an.toCamelCase(), an.toCamelCaseSplit());
      if(list != null && !list.isEmpty()){
        save(list);
        count += list.size();
        page++;
      }else{
        break;
      }
    }
    return count;
  }


  private List<T> transformer(List<Map<String,Object>> list, String columnPrefix, boolean toCamelCase, String toCamelCaseSplit){
    List<T> reList = Lists.newArrayList();
    try {
      for (Map<String, Object> row : list) {
        T domain = metadata.getJavaType().newInstance();
        for (Map.Entry<String, Object> entry : row.entrySet()) {
          String key = entry.getKey().startsWith(columnPrefix)
                       ? entry.getKey().substring(columnPrefix.length()) : entry.getKey();
          Method method = domainSetMethods.get(
            toCamelCase
            ? key.replaceAll(toCamelCaseSplit, "").toUpperCase()
            : key.toUpperCase()
          );
          if(method != null){
            method.invoke(domain, entry.getValue());
          }
        }
        reList.add(domain);
      }
    } catch (Exception e) {
      logger.error("rebuild index transformer error", e);
    }
    return reList;
  }


  private QueryBuilder splitQueryString(String query, String split, List<QueryBuilder> shouldQuery,
                                        List<QueryBuilder> mustQuery, List<QueryBuilder> mustNotQuery){
    QueryBuilder queryBuilder;
    if(StringUtils.isBlank(query)){
      queryBuilder = QueryBuilders.matchAllQuery();
    }else{
      query = query.trim();
      BoolQueryBuilder q = QueryBuilders.boolQuery();
      if(split != null && !"".equals(split)){
        String[] keyArray = StringUtils.split(query, split);
        for (String str : keyArray) {
          q.should(QueryBuilders.matchPhraseQuery("_all", str));
        }
      }else{
        q.should(QueryBuilders.matchPhraseQuery("_all", query));
      }
      if(shouldQuery != null && !shouldQuery.isEmpty()){
        for (QueryBuilder should : shouldQuery) {
          q.should(should);
        }
      }
      if(mustQuery != null && !mustQuery.isEmpty()){
        for (QueryBuilder must : mustQuery) {
          q.must(must);
        }
      }
      if(mustNotQuery != null && !mustNotQuery.isEmpty()){
        for (QueryBuilder mustNot : mustNotQuery) {
          q.mustNot(mustNot);
        }
      }
      queryBuilder = q;
    }
    return queryBuilder;
  }
  
  private String buildNotPersistentSql(List<ID> ids, String domainSql, String idName){
    StringBuilder sql = new StringBuilder("SELECT DISTINCT t.").append(idName).append(" FROM (");
    for (int i = 0; i < ids.size(); i++) {
      if(i > 0){
        sql.append(" UNION ALL ");
      }
      sql.append(" SELECT '").append(ids.get(i)).append("' ").append(idName).append(" ");
    }
    sql.append(") t LEFT JOIN ( ").append(domainSql).append(" ) d ON t.").append(idName)
       .append(" = d.").append(idName).append(" WHERE d.").append(idName).append(" IS NULL");
    return sql.toString();
  }

  public static void setDatabaseExecutor(DatabaseExecutor executor){
    if(DATABASE_EXECUTOR.get() == null && executor != null){
      DATABASE_EXECUTOR.set(executor);
      Logs.get().info("set DatabaseExecutor > " + executor.getClass());
    }
  }

  private static DatabaseExecutor getDatabaseExecutor(){
    Assert.notNull(DATABASE_EXECUTOR.get(), "databaseExecutor is null, must dependency one of database module !");
    return DATABASE_EXECUTOR.get();
  }
}
