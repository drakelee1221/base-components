/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.database.util;



import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

/**
 * 左右值模型计算工具
 *
 * @author DLee
 * @version V1.0
 * @date 2015年9月15日 下午11:05:14
 */
@Component
public class NestedSetModelBuilder {

  /**
   * 重构左右值，使用native sql
   * @param entityManager         -
   * @param parentId              根节点id
   * @param left                  根节点左值，一般为 1
   * @param tableName             表名
   * @param idColumnName          id字段名
   * @param parentIdColumnName    parentId字段名
   * @param leftColumnName        left值字段名
   * @param rightColumnName       right值字段名
   *
   * @return 根节点right值
   */
  @Transactional(rollbackFor = Exception.class)
  public int rebuildWithEntityManager(EntityManager entityManager, String parentId, int left,
                                      String tableName, String idColumnName, String parentIdColumnName,
                                      String leftColumnName, String rightColumnName) {
    int right = left + 1;
    List ids = entityManager
      .createNativeQuery("select "+idColumnName+" from "+tableName+" where "+parentIdColumnName+" = '" + parentId + "'")
      .getResultList();
    for (Object id : ids) {
      right = rebuildWithEntityManager(entityManager, id.toString(), right, tableName, idColumnName,
                                       parentIdColumnName, leftColumnName, rightColumnName);
    }
    entityManager.createNativeQuery(
      "update "+tableName+" set "+leftColumnName+" = " + left + ", "+rightColumnName+" = " + right + " where "+idColumnName+" = '" + parentId + "'")
                      .executeUpdate();
    return right + 1;
  }
}
