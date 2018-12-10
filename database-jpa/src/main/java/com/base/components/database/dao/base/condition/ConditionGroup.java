/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.database.dao.base.condition;

import com.google.common.collect.Iterables;
import com.base.components.common.util.SqlLikeHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * JPA条件组
 * <pre>
 * 示例：
 *    查询实体关联对象child :  child.name = 1
 *    ConditionGroup.build()
 *      .addCondition("child.name", ConditionEnum.OPERATE_EQUAL, 1)
 *
 *    hql:  a = 1 AND ( b = 2 OR c = 3)
 *    ConditionGroup.build()
 *      .addCondition("a", ConditionEnum.OPERATE_EQUAL, 1)
 *      .addGroup(
 *        ConditionGroup.build(ConditionEnum.Link.LINK_OR)
 *           .addCondition("b", ConditionEnum.OPERATE_EQUAL, 2)
 *           .addCondition("c", ConditionEnum.OPERATE_EQUAL, 3)
 *      )
 *
 *
 *    hql:  a = 1 OR b = 2
 *    ConditionGroup.build(ConditionEnum.Link.LINK_OR)
 *      .addCondition("a", ConditionEnum.OPERATE_EQUAL, 1)
 *      .addCondition("b", ConditionEnum.OPERATE_EQUAL, 2)
 *
 *
 *    hql: ( a = 1 AND b = 2) OR ( c = 3 AND d = 4)
 *    ConditionGroup.build(ConditionEnum.Link.LINK_OR)
 *      .addGroup(
 *        ConditionGroup.build()
 *           .addCondition("a", ConditionEnum.OPERATE_EQUAL, 1)
 *           .addCondition("b", ConditionEnum.OPERATE_EQUAL, 2)
 *      )
 *      .addGroup(
 *        ConditionGroup.build()
 *           .addCondition("c", ConditionEnum.OPERATE_EQUAL, 3)
 *           .addCondition("d", ConditionEnum.OPERATE_EQUAL, 4)
 *      )
 *
 * </pre>
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2017-12-29 12:56
 *
 */
public class ConditionGroup<T> implements Serializable, Specification<T> {
  private static final long serialVersionUID = 285804017306048945L;
  private List<ConditionGroup> groups = new ArrayList<>();
  private List<Condition> conditions = new ArrayList<Condition>();

  /** 链接符, 默认AND */
  private ConditionEnum.Link linkFlag = ConditionEnum.Link.LINK_AND;

  /**
   * 创建AND连接符条件组
   */
  public static <T> ConditionGroup<T> build() {
    return new ConditionGroup<>();
  }

  /**
   * 根据连接符创建条件组
   */
  public static <T> ConditionGroup<T> build(ConditionEnum.Link link) {
    ConditionGroup<T> group = new ConditionGroup<>();
    group.setLinkFlag(link);
    return group;
  }

  private ConditionGroup() {
  }


  /**
   * 添加查询条件, 默认为AND连接
   *
   * @param conditionName 实体类属性名
   * @param conditionValue 值为可序列化任意对象或集合
   * @param operateFlag 操作符
   * @param checkValueEmpty 是否验证为空，或空集合，非空时才添加条件
   */
  @SuppressWarnings("all")
  public <T> ConditionGroup<T> addCondition(String conditionName, ConditionEnum operateFlag, Serializable conditionValue, boolean checkValueEmpty) {
    boolean checked = false;
    if(checkValueEmpty){
      if(conditionValue != null){
        if(conditionValue instanceof Iterable){
          Iterable it = (Iterable) conditionValue;
          if(!Iterables.isEmpty(it)){
            checked = true;
          }
        }
        else if(StringUtils.isNotBlank(conditionValue.toString())){
          checked = true;
        }
      }
    }else{
      checked = true;
    }
    if(checked){
      checkFlag(operateFlag);
      Condition con = new Condition(conditionName, conditionValue, operateFlag);
      conditions.add(con);
    }
    return (ConditionGroup<T>) this;
  }


  /**
   * 添加查询条件
   *
   * @param conditionName 实体类属性名
   * @param conditionValue 值为可序列化任意对象或集合
   * @param operateFlag 操作符
   */
  @SuppressWarnings("all")
  public <T> ConditionGroup<T> addCondition(String conditionName, ConditionEnum operateFlag, Serializable conditionValue) {
    checkFlag(operateFlag);
    Condition con = new Condition(conditionName, conditionValue, operateFlag);
    conditions.add(con);
    return (ConditionGroup<T>) this;
  }

  /**
   * 添加查询条件
   *
   * @param conditionName 实体类属性名
   * @param conditionValue 值为可序列化任意对象或集合
   * @param operateFlag 操作符
   */
  @SuppressWarnings("all")
  public <T> ConditionGroup<T> addGroup(ConditionGroup... otherGroups){
    for (ConditionGroup otherGroup : otherGroups) {
      this.groups.add(otherGroup);
    }
    return (ConditionGroup<T>) this;
  }

  private void checkFlag(ConditionEnum flag) {
    if (flag == null) {
      throw new IllegalArgumentException("传入参数错误(operate), 操作符不能为 null !");
    }
  }

  public List<Condition> getConditions() {
    return conditions;
  }

  public List<ConditionGroup> getGroups() {
    return groups;
  }

  public ConditionEnum.Link getLinkFlag() {
    return linkFlag;
  }

  public void setLinkFlag(ConditionEnum.Link linkFlag) {
    if(linkFlag != null){
      this.linkFlag = linkFlag;
    }
  }


  @Override
  @SuppressWarnings("all")
  public Predicate toPredicate(Root root, CriteriaQuery query, CriteriaBuilder cb) {
    Predicate p = buildPredicate(this, root, query, cb);
    return p;
  }

  @SuppressWarnings("all")
  private static <T> Predicate buildPredicate(ConditionGroup<T> group, Root root, CriteriaQuery query, CriteriaBuilder cb){
    List<Predicate> pList = new ArrayList<Predicate>();
    //循环单个条件
    for (Condition con : group.getConditions()) {
      Predicate p = null;
      Path key;
      if (con.getConditionName().contains(".")) {
        String[] names = StringUtils.split(con.getConditionName(), ".");
        key = root.get(names[0]);
        for (int i = 1; i < names.length; i++) {
          key = key.get(names[i]);
        }
      } else {
        key = root.get(con.getConditionName());
      }
      switch (con.getOperateFlag()) {
        case OPERATE_IS_NOT_NULL:
          p = cb.isNotNull(key);
          break;
        case OPERATE_IS_NULL:
          p = cb.isNull(key);
          break;
        case OPERATE_EQUAL:
          p = cb.equal(key, con.getConditionValue());
          break;
        case OPERATE_UNEQUAL:
          p = cb.notEqual(key, con.getConditionValue());
          break;
        case OPERATE_GREATER:
          p = cb.greaterThan(key, (Comparable) con.getConditionValue());
          break;
        case OPERATE_LESS:
          p = cb.lessThan(key, (Comparable) con.getConditionValue());
          break;
        case OPERATE_GREATER_EQUAL:
          p = cb.greaterThanOrEqualTo(key, (Comparable) con.getConditionValue());
          break;
        case OPERATE_LESS_EQUAL:
          p = cb.lessThanOrEqualTo(key, (Comparable) con.getConditionValue());
          break;
        case OPERATE_IN:
          p = key.in((Collection) con.getConditionValue());
          break;
        case OPERATE_NOT_IN:
          p = key.in((Collection) con.getConditionValue()).not();
          break;
        case OPERATE_LIKE:
          p = cb.like(key, con.getConditionValue() == null ? "null" : SqlLikeHelper.leftAndRight(con.getConditionValue().toString()));
          break;
        case OPERATE_LEFT_LIKE:
          p = cb.like(key, con.getConditionValue() == null ? "null" : SqlLikeHelper.left(con.getConditionValue().toString()));
          break;
        case OPERATE_RIGHT_LIKE:
          p = cb.like(key, con.getConditionValue() == null ? "null" : SqlLikeHelper.right(con.getConditionValue().toString()));
          break;
        default:
          break;
      }
      if (p != null) {
        pList.add(p);
      }
    }
    //循环条件组
    for (ConditionGroup conditionGroup : group.getGroups()) {
      Predicate pre = buildPredicate(conditionGroup, root, query, cb);
      if(pre != null){
        pList.add(pre);
      }
    }
    if (pList.size() > 0) {
      if(ConditionEnum.Link.LINK_AND.equals(group.getLinkFlag())){
        return cb.and(pList.toArray(new Predicate[pList.size()]));
      }else{
        return cb.or(pList.toArray(new Predicate[pList.size()]));
      }
    }
    return null;
  }


  /** 查询条件 */
  static class Condition implements Serializable {
    private static final long serialVersionUID = -1450665408301275949L;

    /** 条件名 */
    private String conditionName;
    /** 条件值 */
    private Serializable conditionValue;
    /** 操作符 */
    private ConditionEnum operateFlag;


    public Condition() {

    }

    Condition(String conditionName, Serializable conditionValue, ConditionEnum operateFlag) {
      this.conditionName = conditionName;
      this.conditionValue = conditionValue;
      this.operateFlag = operateFlag;
    }

    public String getConditionName() {
      return conditionName;
    }

    public void setConditionName(String conditionName) {
      this.conditionName = conditionName;
    }

    public Object getConditionValue() {
      return conditionValue;
    }

    public void setConditionValue(Serializable conditionValue) {
      this.conditionValue = conditionValue;
    }

    public ConditionEnum getOperateFlag() {
      return operateFlag;
    }

    public void setOperateFlag(ConditionEnum operateFlag) {
      this.operateFlag = operateFlag;
    }

  }
}