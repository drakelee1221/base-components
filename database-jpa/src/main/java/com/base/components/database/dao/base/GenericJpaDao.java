/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.database.dao.base;

import com.base.components.common.constants.sys.Pages;
import com.base.components.common.dto.page.DataPage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.Serializable;

/**
 * @param <T> 实体类
 * @param <ID> 实体类ID
 *
 * @author DLee
 * @version V1.0
 * @Description 抽象各种继承自Spring Data JPA的各种DAO, 用于在JPAGenericAbstractService中统一调用
 * @date 2015年9月5日 下午3:32:55
 */
@NoRepositoryBean
public interface GenericJpaDao<T, ID extends Serializable>
  extends Serializable,JpaRepository<T, ID>, JpaSpecificationExecutor<T> {

  /**
   * @return EntityManager
   */
  EntityManager getEntityManager();

  /**
   * 设置Query查询返回值为Map类型
   * @param listQuery - Nonnull  - 列表查询Query
   * @return Query
   */
  Query setMapResult(Query listQuery);

  /**
   * Query查询分页
   * @param countQuery  - Nonnull  - 总记录数查询Query
   * @param listQuery   - Nonnull  - 列表查询Query
   * @param pageNum     - Nullable - 当前页数，默认{@link Pages#PAGE_NUM}
   * @param pageSize    - Nullable - 每页行数，默认{@link Pages#PAGE_SIZE}
   *
   * @return DataPage
   */
  DataPage<T> pageByQuery(Query countQuery, Query listQuery, Integer pageNum, Integer pageSize);
}
