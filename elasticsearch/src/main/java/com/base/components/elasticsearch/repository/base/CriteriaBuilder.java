/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.elasticsearch.repository.base;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.elasticsearch.core.query.Criteria;

/**
 * CriteriaBuilder
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-02-13 14:17
 */
public class CriteriaBuilder {

  /**
   * 对已有criteria对象，增加动态 AND 条件
   * @param criteria 非空的criteria对象
   * @param field 增加条件的字段
   * @param val 字段值
   * @param opKey 连接符
   * @return 原 criteria
   */
  public static Criteria andCriteria(Criteria criteria, String field, Object val, Criteria.OperationKey opKey){
    if(val!=null && !StringUtils.isBlank(val.toString())){
      switch (opKey){
        case IN:
          criteria.and(new Criteria(field).in((Iterable) val));
          break;
        case LESS:
          criteria.and(new Criteria(field).lessThan(val));
          break;
        case GREATER:
          criteria.and(new Criteria(field).greaterThan(val));
          break;
        case LESS_EQUAL:
          criteria.and(new Criteria(field).lessThanEqual(val));
          break;
        case GREATER_EQUAL:
          criteria.and(new Criteria(field).greaterThanEqual(val));
          break;
        case FUZZY:
          criteria.and(new Criteria(field).fuzzy(val.toString()));
          break;
        case EQUALS:
          criteria.and(new Criteria(field).is(val));
          break;
        case NOT_IN:
          criteria.and(new Criteria(field).notIn(val));
          break;
        case CONTAINS:
          criteria.and(new Criteria(field).contains(val.toString()));
          break;
        default:
          break;
      }
    }
    return criteria;
  }
}
