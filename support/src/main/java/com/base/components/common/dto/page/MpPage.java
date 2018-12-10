
package com.base.components.common.dto.page;

import com.baomidou.mybatisplus.plugins.Page;

/**
 * MpPage  mybatisPlus - Page
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-06-11 10:42
 */
public class MpPage<T> extends Page<T> {
  private static final long serialVersionUID = -5451142420952294556L;

  public MpPage() {
  }

  public MpPage(int current, int size) {
    super(current, size);
  }

  public MpPage(int current, int size, String orderByField) {
    super(current, size, orderByField);
  }

  public MpPage(int current, int size, String orderByField, boolean isAsc) {
    super(current, size, orderByField, isAsc);
  }

  public DataPage<T> toDataPage(){
//    List<T> records = getRecords();
//    if(records == null || records.isEmpty()){
//      return DataPage.getEmpty();
//    }
    DataPage<T> page = new DataPage<>();
    page.setPageNum(getCurrent() < 0 ? 1 : getCurrent() + 1);
    page.setPageSize(getSize());
    page.setTotal(getTotal());
    page.setPages(getPages() < 0 ? 1 : (int)getPages());
    page.setList(getRecords());
    return page;
  }
}
