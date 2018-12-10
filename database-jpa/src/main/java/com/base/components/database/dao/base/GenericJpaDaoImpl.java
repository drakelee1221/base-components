/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.database.dao.base;

import com.base.components.common.constants.sys.Pages;
import com.base.components.common.dto.page.DataPage;
import org.hibernate.transform.Transformers;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.Serializable;
import java.util.List;

/**
 * @param <T> 实体类
 * @param <ID> 实体类ID
 *
 * @author DLee
 * @version V1.0
 * @Description JPA基础Dao实现
 * @date 2015年9月5日 下午3:32:55
 */
@NoRepositoryBean
public class GenericJpaDaoImpl<T, ID extends Serializable> extends SimpleJpaRepository<T, ID>
  implements GenericJpaDao<T, ID> {

  private EntityManager entityManager;

  public GenericJpaDaoImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
    super(entityInformation, entityManager);
  }

  public GenericJpaDaoImpl(Class<T> domainClass, EntityManager em) {
    super(domainClass, em);
    this.entityManager = em;
  }

  @Override
  public EntityManager getEntityManager() {
    return entityManager;
  }

  @Override
  public Query setMapResult(Query listQuery){
    listQuery.unwrap(org.hibernate.query.Query.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
    return listQuery;
  }


  @Override
  @SuppressWarnings("all")
  public DataPage<T> pageByQuery(Query countQuery, Query listQuery, Integer pageNum, Integer pageSize){
    DataPage<T> page = DataPage.getEmpty();
    int _pageNum = Pages.Helper.pageNum(pageNum == null ? 0 : pageNum);
    int _pageSize = Pages.Helper.pageSize(pageSize == null ? 0 : pageSize);
    long count = Long.parseLong(countQuery.getSingleResult().toString());
    if(count > 0){
      listQuery.setFirstResult((_pageNum - 1) * _pageSize);
      listQuery.setMaxResults(_pageSize);
      List<T> list = listQuery.getResultList();
      int pages = (int)count / _pageSize;
      if (pages == 0) {
        pages = 1;
      } else {
        if ((count % _pageSize) != 0) {
          pages++;
        }
      }
      page = new DataPage<>();
      page.setList(list);
      page.setPageNum(_pageNum);
      page.setPageSize(_pageSize);
      page.setTotal(count);
      page.setPages(pages);
    }
    return page;
  }
}
