/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.database.dao.base;

import com.base.components.database.dao.base.condition.ConditionEnum;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.HashMap;
import java.util.Map;

/**
 * 构建原生sql动态条件
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2017-11-28 16:57
 */
public class NativeQueryBuilder {
  private StringBuilder whereSql = new StringBuilder();
  private String querySql;
  private Map<String, Object> paramMap = new HashMap<>();
  private String cur = "";
  private String orderBy = "";
  public NativeQueryBuilder(){
  }

  public NativeQueryBuilder addWhere(String prefix, String field, Object value, ConditionEnum opFlag, String ...optional) {
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
        if (value != null && StringUtils.isNotBlank(value.toString())) {
          whereSql.append(cur).append(prefix).append(".").append(field).append(" =:").append(paramField);
          paramMap.put(paramField, value);
        }

        break;
      case OPERATE_UNEQUAL:
        if (value != null && StringUtils.isNotBlank(value.toString())) {
          whereSql.append(cur).append(prefix).append(".").append(field).append(" !=:").append(paramField);
          paramMap.put(paramField, value);
        }
        break;
      case OPERATE_GREATER:
        if (value != null && StringUtils.isNotBlank(value.toString())) {
          whereSql.append(cur).append(prefix).append(".").append(field).append(" >:").append(paramField);
          paramMap.put(paramField, value);
        }
        break;
      case OPERATE_LESS:
        if (value != null && StringUtils.isNotBlank(value.toString())) {
          whereSql.append(cur).append(prefix).append(".").append(field).append(" <:").append(paramField);
          paramMap.put(paramField, value);
        }
        break;
      case OPERATE_GREATER_EQUAL:
        if (value != null && StringUtils.isNotBlank(value.toString())) {
          whereSql.append(cur).append(prefix).append(".").append(field).append(" >=:").append(paramField);
          paramMap.put(paramField, value);
        }
        break;
      case OPERATE_LESS_EQUAL:
        if (value != null && StringUtils.isNotBlank(value.toString())) {
          whereSql.append(cur).append(prefix).append(".").append(field).append(" <=:").append(paramField);
          paramMap.put(paramField, value);
        }
        break;
      case OPERATE_IN:
        if (value != null && StringUtils.isNotBlank(value.toString())) {
          whereSql.append(cur).append(prefix).append(".").append(field).append(" in (:").append(paramField).append(")");
          paramMap.put(paramField, value);
        }
        break;
      case OPERATE_NOT_IN:
        if (value != null && StringUtils.isNotBlank(value.toString())) {
          whereSql.append(cur).append(prefix).append(".").append(field).append(" not in (:").append(paramField).append(")");
          paramMap.put(paramField, value);
        }
        break;
      case OPERATE_LIKE:
        if (value != null && StringUtils.isNotBlank(value.toString())) {
          whereSql.append(cur).append(prefix).append(".").append(field).append(" like ").append(":").append(paramField)
                  .append("");
          paramMap.put(paramField, "%" + value + "%");
        }
        break;
      case OPERATE_LEFT_LIKE:
        if (value != null && StringUtils.isNotBlank(value.toString())) {
          whereSql.append(cur).append(prefix).append(".").append(field).append(" like ").append(":").append(paramField)
                  .append("");
          paramMap.put(paramField, value + "%");
        }
        break;
      case OPERATE_RIGHT_LIKE:
        if (value != null && StringUtils.isNotBlank(value.toString())) {
          whereSql.append(cur).append(prefix).append(".").append(field).append(" like ").append(":").append(paramField)
                  .append("");
          paramMap.put(paramField, "%" + value);
        }
        break;
      default:
        break;
    }
    return this;
  }

  public NativeQueryBuilder or() {
    cur = " or ";
    return this;
  }

  public NativeQueryBuilder and() {
    cur = " and ";
    return this;
  }

  public NativeQueryBuilder orGroupStart() {
    whereSql.append(" or (");
    return this;
  }

  public NativeQueryBuilder andGroupStart() {
    whereSql.append(" and (");
    return this;
  }

  public NativeQueryBuilder groupEnd() {
    whereSql.append(" ) ");
    return this;
  }
  public NativeQueryBuilder orderBy(String orderBySql) {
    this.orderBy = orderBySql;
    return this;
  }
  public Query build(EntityManager entityManager) {
    String temp = querySql + this.whereSql.toString();
    if(StringUtils.isNotBlank(orderBy)){
      temp+=" order by "+this.orderBy;
    }
    Query nativeQuery = entityManager.createNativeQuery(temp);
    for (Map.Entry en : paramMap.entrySet()) {
      nativeQuery.setParameter(en.getKey().toString(), en.getValue());
    }
    return nativeQuery;
  }

  public NativeQueryBuilder bindQuerySql(String querySql) {
    this.querySql = querySql;
    return this;
  }
}
