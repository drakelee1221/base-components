package com.base.components.common.constants.sys;

import static com.base.components.common.constants.sys.Pages.*;

/**
 * PageHelper
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-09-12 14:38
 */
public class UnlimitedPageHelper extends PageHelper{

  public static final UnlimitedPageHelper INSTANCE = new UnlimitedPageHelper();

  private UnlimitedPageHelper(){
    super();
  }

  /**
   * 返回不大于{@link Pages#MAX}的每页行数
   *
   * @param pageSize 参数传入的每页行数
   */
  @Override
  public int pageSize(int pageSize) {
    return pageSize(pageSize, MAX);
  }

  /**
   * 返回不大于{@link Pages#MAX}的每页行数
   *
   * @param pageSize 参数传入的每页行数
   */
  @Override
  public int pageSize(String pageSize) {
    return pageSize(pageSize, MAX);
  }

}
