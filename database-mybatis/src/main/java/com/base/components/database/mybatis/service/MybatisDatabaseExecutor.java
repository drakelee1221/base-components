package com.base.components.database.mybatis.service;

import com.baomidou.mybatisplus.plugins.pagination.PageHelper;
import com.base.components.common.service.database.DatabaseExecutor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Map;

/**
 * DatabaseExecutor
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-07-27 9:52
 */
public class MybatisDatabaseExecutor implements DatabaseExecutor {

  @Autowired
  private SqlSessionFactory sqlSessionFactory;

  @Override
  public <ID> List<ID> queryIds(@NonNull String sql){
    return sqlSessionFactory.openSession().selectList(sql);
  }

  @Override
  public List<Map<String, Object>> queryRows(@NonNull String sql, int page, int pageSize){
    PageHelper.startPage(page, pageSize);
    return sqlSessionFactory.openSession().selectList(sql);
  }
}
