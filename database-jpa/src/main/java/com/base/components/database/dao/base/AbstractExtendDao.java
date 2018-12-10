/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.database.dao.base;

import com.base.components.common.constants.sys.Pages;
import com.base.components.common.dto.page.DataPage;
import org.hibernate.transform.AliasedTupleSubsetResultTransformer;
import org.hibernate.transform.ResultTransformer;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 所有自定义拓展dao实现需继承此类
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2017-11-28 11:06
 */
public abstract class AbstractExtendDao<T> {
  private static ResultTransformer linkedMapTransformer = new AliasedTupleSubsetResultTransformer(){
    @Override
    public Object transformTuple(Object[] tuple, String[] aliases) {
      Map result = new LinkedHashMap(tuple.length);

      for(int i = 0; i < tuple.length; ++i) {
        String alias = aliases[i];
        if(alias != null) {
          result.put(alias, tuple[i]);
        }
      }

      return result;
    }

    @Override
    public boolean isTransformedValueATupleElement(String[] strings, int i) {
      return false;
    }
  };
  @PersistenceContext
  protected EntityManager entityManager;

  public Query setMapResult(Query listQuery){
    listQuery.unwrap(org.hibernate.query.Query.class).setResultTransformer(linkedMapTransformer);
    return listQuery;
  }

  public Query setTransformerResult(Query listQuery, ResultTransformer resultTransformer){
    listQuery.unwrap(org.hibernate.Query.class).setResultTransformer(resultTransformer);
    return listQuery;
  }

  @SuppressWarnings("all")
  public DataPage<T> pageByQuery(Query countQuery, Query listQuery, Integer pageNum, Integer pageSize){

    int _pageNum = Pages.Helper.pageNum(pageNum == null ? 0 : pageNum);
    int _pageSize = Pages.Helper.pageSize(pageSize == null ? 0 : pageSize);
    return pageByQueryNoHelper(countQuery, listQuery, _pageNum, _pageSize);
  }

  public DataPage<T> pageByQueryNoHelper(Query countQuery, Query listQuery, int pageNum, int pageSize) {
    DataPage<T> page = DataPage.getEmpty();
    long count = Long.parseLong(countQuery.getSingleResult().toString());
    if(count > 0){
      listQuery.setFirstResult((pageNum - 1) * pageSize);
      listQuery.setMaxResults(pageSize);
      List<T> list = listQuery.getResultList();
      int pages = (int)count / pageSize;
      if (pages == 0) {
        pages = 1;
      } else {
        if ((count % pageSize) != 0) {
          pages++;
        }
      }
      page = new DataPage<>();
      page.setList(list);
      page.setPageNum(pageNum);
      page.setPageSize(pageSize);
      page.setTotal(count);
      page.setPages(pages);
    }
    return page;
  }

}
