package com.base.components.database.service;

import com.base.components.common.service.database.DatabaseExecutor;
import org.hibernate.transform.Transformers;
import org.springframework.lang.NonNull;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;
import java.util.Map;

/**
 * DatabaseExecutor
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-07-27 9:52
 */
public class JpaDatabaseExecutor implements DatabaseExecutor {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  @SuppressWarnings("unchecked")
  public <ID> List<ID> queryIds(@NonNull String sql){
    return entityManager.createNativeQuery(sql).getResultList();
  }

  @Override
  @SuppressWarnings("all")
  public List<Map<String, Object>> queryRows(@NonNull String sql, int page, int pageSize){
    Query query = entityManager.createNativeQuery(sql).setFirstResult((page - 1) * pageSize).setMaxResults(pageSize);
    query.unwrap(org.hibernate.query.Query.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
    return query.getResultList();
  }
}
