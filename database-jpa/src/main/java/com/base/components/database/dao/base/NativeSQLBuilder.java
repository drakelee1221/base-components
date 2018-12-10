/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.database.dao.base;

import com.base.components.common.util.SqlLikeHelper;
import com.base.components.database.dao.base.condition.ConditionEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.HashMap;
import java.util.Map;

/**
 * 构建原生sql动态条件
 *
 * @author <a href="jiang2008wen@126.com">JiangWen</a>
 * @version 1.0.0, 2017-11-28 16:57
 */
public class NativeSQLBuilder {
  private StringBuilder whereSql = new StringBuilder();
  private String querySql;
  private Map<String, Object> paramMap = new HashMap<>();
  private String cur = "";
  private String orderBy = "";
  private String groupBy = "";
  public NativeSQLBuilder(){
  }

  public NativeSQLBuilder addWhere(String prefix, String field, Object value, ConditionEnum opFlag, String ...optional) {
    String paramField = field;
    if(optional.length>0){
      paramField = optional[0];
    }

    switch (opFlag) {
      case OPERATE_IS_NOT_NULL:
        whereSql.append(cur).append(prefix).append(".").append(field).append(" is not null");
        break;
      case OPERATE_IS_NULL:
        whereSql.append(cur).append(prefix).append(".").append(field).append(" is null");
        break;
      case OPERATE_EQUAL:
        if (value != null) {
          whereSql.append(cur).append(prefix).append(".").append(field).append(" =:").append(paramField);
          paramMap.put(paramField, value);
        }

        break;
      case OPERATE_UNEQUAL:
        if (value != null) {
          whereSql.append(cur).append(prefix).append(".").append(field).append(" !=:").append(paramField);
          paramMap.put(paramField, value);
        }
        break;
      case OPERATE_GREATER:
        if (value != null) {
          whereSql.append(cur).append(prefix).append(".").append(field).append(" >:").append(paramField);
          paramMap.put(paramField, value);
        }
        break;
      case OPERATE_LESS:
        if (value != null) {
          whereSql.append(cur).append(prefix).append(".").append(field).append(" <:").append(paramField);
          paramMap.put(paramField, value);
        }
        break;
      case OPERATE_GREATER_EQUAL:
        if (value != null) {
          whereSql.append(cur).append(prefix).append(".").append(field).append(" >=:").append(paramField);
          paramMap.put(paramField, value);
        }
        break;
      case OPERATE_LESS_EQUAL:
        if (value != null) {
          whereSql.append(cur).append(prefix).append(".").append(field).append(" <=:").append(paramField);
          paramMap.put(paramField, value);
        }
        break;
      case OPERATE_IN:
        if (value != null) {
          whereSql.append(cur).append(prefix).append(".").append(field).append(" in (:").append(paramField).append(")");
          paramMap.put(paramField, value);
        }
        break;
      case OPERATE_NOT_IN:
        if (value != null) {
          whereSql.append(cur).append(prefix).append(".").append(field).append(" not in (:").append(paramField).append(")");
          paramMap.put(paramField, value);
        }
        break;
      case OPERATE_LIKE:
        if (value != null) {
          whereSql.append(cur).append(prefix).append(".").append(field).append(" like ").append(":").append(paramField);
          paramMap.put(paramField, SqlLikeHelper.leftAndRight(value.toString()));
        }
        break;
      case OPERATE_LEFT_LIKE:
        if (value != null) {
          whereSql.append(cur).append(prefix).append(".").append(field).append(" like ").append(":").append(paramField);
          paramMap.put(paramField, SqlLikeHelper.left(value.toString()));
        }
        break;
      case OPERATE_RIGHT_LIKE:
        if (value != null) {
          whereSql.append(cur).append(prefix).append(".").append(field).append(" like ").append(":").append(paramField);
          paramMap.put(paramField, SqlLikeHelper.right(value.toString()));
        }
        break;
      default:
        break;
    }
    return this;
  }

  public NativeSQLBuilder or() {
    whereSql.append(" or ");
    return this;
  }

  public NativeSQLBuilder and() {
    whereSql.append(" and ");
    return this;
  }

  public NativeSQLBuilder orGroupStart() {
    whereSql.append(" or (");
    return this;
  }

  public NativeSQLBuilder andGroupStart() {
    whereSql.append(" and (");
    return this;
  }

  public NativeSQLBuilder groupStart() {
    whereSql.append(" ( ");
    return this;
  }

  public NativeSQLBuilder groupEnd() {
    whereSql.append(" ) ");
    return this;
  }
  public NativeSQLBuilder orderBy(String orderBySql) {
    this.orderBy = orderBySql;
    return this;
  }
  public NativeSQLBuilder groupBy(String groupBySql) {
    this.groupBy = groupBySql;
    return this;
  }
  public Query build(EntityManager entityManager) {
    String temp = querySql + this.whereSql.toString();
    if(StringUtils.isNotBlank(groupBy)){
      temp+=" group by "+this.groupBy;
    }
    if(StringUtils.isNotBlank(orderBy)){
      temp+=" order by "+this.orderBy;
    }
    Query nativeQuery = entityManager.createNativeQuery(temp);
    for (Map.Entry en : paramMap.entrySet()) {
      nativeQuery.setParameter(en.getKey().toString(), en.getValue());
    }
    return nativeQuery;
  }

  public Query build(EntityManager entityManager,String includeSql,String placeholder) {
    String temp = includeSql.replace(placeholder, querySql + this.whereSql.toString());
    if(StringUtils.isNotBlank(groupBy)){
      temp+=" group by "+this.groupBy;
    }
    if(StringUtils.isNotBlank(orderBy)){
      temp+=" order by "+this.orderBy;
    }
    Query nativeQuery = entityManager.createNativeQuery(temp);
    for (Map.Entry en : paramMap.entrySet()) {
      nativeQuery.setParameter(en.getKey().toString(), en.getValue());
    }
    return nativeQuery;
  }

  public void setParameter(String key, Object value){
    Assert.isTrue(StringUtils.isNotBlank(key), "key can not be null");
    Assert.isTrue(value != null, "value can not be null");
    this.paramMap.put(key, value);
  }

  public NativeSQLBuilder bindQuerySql(String querySql) {
    this.querySql = querySql;
    return this;
  }
}
