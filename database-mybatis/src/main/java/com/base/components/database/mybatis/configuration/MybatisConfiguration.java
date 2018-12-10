package com.base.components.database.mybatis.configuration;

import com.baomidou.mybatisplus.incrementer.H2KeyGenerator;
import com.baomidou.mybatisplus.incrementer.IKeyGenerator;
import com.baomidou.mybatisplus.mapper.ISqlInjector;
import com.baomidou.mybatisplus.mapper.LogicSqlInjector;
import com.baomidou.mybatisplus.plugins.PaginationInterceptor;
import com.base.components.common.service.database.DatabaseExecutor;
import com.base.components.database.mybatis.service.MybatisDatabaseExecutor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * MybatisConfiguration - 继承该配置类，并使用如下注解
 * <pre>
 *   {@code
 *      @EnableTransactionManagement
        @Configuration
        @MapperScan("com.mj.he800.database.mybatis.mapper")
 *   }
 * </pre>
 *
 * @author <a href="morse.jiang@foxmail.com">JiangWen</a>
 * @version 1.0.0, 2018/6/5 0005 10:31
 */
public class MybatisConfiguration {

  /**
   * 分页插件
   */
  @Bean
  public PaginationInterceptor paginationInterceptor() {
    PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
    // 开启 PageHelper 的支持
    paginationInterceptor.setLocalPage(true);
    return paginationInterceptor;
  }

  /**
   * 注入主键生成器
   */
  @Bean
  public IKeyGenerator keyGenerator(){
    return new H2KeyGenerator();
  }

  /**
   * 注入sql注入器
   */
  @Bean
  public ISqlInjector sqlInjector(){
    return new LogicSqlInjector();
  }


  @Bean
  @ConditionalOnMissingBean
  public DatabaseExecutor mybatisDatabaseExecutor(){
    return new MybatisDatabaseExecutor();
  }
}
