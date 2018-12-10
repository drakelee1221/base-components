package com.base.components.common.constants.sys;


import org.springframework.data.domain.Pageable;

/**
 * 分页工具
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2017-11-13 15:20
 */
@SuppressWarnings("all")
public interface Pages {

  /**
   * 用于  org.springframework.data.jpa.repository.Query 注解的Jpa接口分页方法后缀；<br>
   * 如：@Query(value = "select 1 from table where a = 1 " + PAGEABLE_FLAG, nativeQuery = true)    <br>
   * 接口方法参数需带有 {@link Pageable}
   */
  //String PAGEABLE_FLAG = " \n#pageable\n";

  String PAGE_NUM_VAR_NAME = "pageNum";

  String PAGE_SIZE_VAR_NAME = "pageSize";

  /**
   * 默认页数
   */
  int PAGE_NUM = 1;

  /**
   * 默认每页条数
   */
  int PAGE_SIZE = 30;

  /**
   * 最大每页条数
   */
  int MAX = 10000;

  /** 分页参数工具， 限制每页上限为 {@link #PAGE_SIZE} */
  PageHelper Helper = PageHelper.INSTANCE;

  /** 分页参数工具， 限制每页上限为 {@link #MAX} */
  UnlimitedPageHelper UnlimitedHelper = UnlimitedPageHelper.INSTANCE;

}
