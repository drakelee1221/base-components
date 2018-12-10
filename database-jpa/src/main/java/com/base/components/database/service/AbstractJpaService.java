/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.database.service;

import com.base.components.common.constants.sys.Pages;
import com.base.components.common.dto.page.DataPage;
import com.base.components.database.dao.base.GenericJpaDao;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.Serializable;
import java.util.List;

/**
 * @param <T> 实体类
 * @param <ID> 实体类ID
 *
 * @author DLee
 * @version V1.0
 * @Description 实现service(JPAGenericInterfaceService)中通用的CRUD方法和一些工具方法, 各service的实现类需继承此类
 * @date 2015年9月5日 下午3:12:09
 */
public abstract class AbstractJpaService<T extends Serializable, ID extends Serializable, Dao extends GenericJpaDao<T, ID>> 
  implements JpaService<T, ID> {

  @PersistenceContext
  private EntityManager entityManager;

  protected AbstractJpaService() {
  }
  protected EntityManager getEntityManager(){
    return this.entityManager;
  }

  /**
   * 获取具体Dao实现
   *
   * @return GenericJpaDao
   */
  @Autowired
  private Dao jpaDao;

  protected Dao getDao(){
    return jpaDao;
  }
  
  @Override
  public List<T> findAll() {
    return getDao().findAll();
  }
  @Override
  public List<T> findAll(Sort paramSort) {
    return getDao().findAll(paramSort);
  }
  @Override
  public List<T> findAllById(Iterable<ID> paramIterable) {
    return getDao().findAllById(paramIterable);
  }
  @Override
  public T getOne(ID paramID) {
    return getDao().getOne(paramID);
  }
  @Override
  public long count(Specification<T> paramSpecification) {
    return getDao().count(paramSpecification);
  }
  @Override
  public T findById(ID paramID) {
    return getDao().findById(paramID).orElse(null);
  }
  @Override
  public boolean existsById(ID paramID) {
    return getDao().existsById(paramID);
  }
  @Override
  public long count() {
    return getDao().count();
  }
  @Override
  public Page<T> findAll(Pageable paramPageable) {
    return getDao().findAll(paramPageable);
  }
  @Override
  public T findOne(Specification<T> paramSpecification) {
    return getDao().findOne(paramSpecification).orElse(null);
  }
  @Override
  public List<T> findAll(Specification<T> paramSpecification) {
    return getDao().findAll(paramSpecification);
  }
  @Override
  public Page<T> findAll(Specification<T> paramSpecification, Pageable paramPageable) {
    return getDao().findAll(paramSpecification, paramPageable);
  }
  @Override
  public List<T> findAll(Specification<T> paramSpecification, Sort paramSort) {
    return getDao().findAll(paramSpecification, paramSort);
  }

  @Override
  public Query setMapResult(Query listQuery) {
    listQuery.unwrap(org.hibernate.query.Query.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
    return listQuery;
  }


  @SuppressWarnings("all")
  @Override
  public DataPage<T> pageByQuery(Query countQuery, Query listQuery, Integer pageNum, Integer pageSize) {
    DataPage<T> page = DataPage.getEmpty();
    int _pageNum = Pages.Helper.pageNum(pageNum == null ? 0 : pageNum);
    int _pageSize = Pages.Helper.pageSize(pageSize == null ? 0 : pageSize);
    long count = Long.parseLong(countQuery.getSingleResult().toString());
    if (count > 0) {
      listQuery.setFirstResult((_pageNum - 1) * _pageSize);
      listQuery.setMaxResults(_pageSize);
      List<T> list = listQuery.getResultList();
      int pages = (int) count / _pageSize;
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
