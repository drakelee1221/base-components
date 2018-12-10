
package com.base.components.common.dto.page;

import com.base.components.common.constants.sys.Pages;
import com.base.components.common.doc.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 返回给前端的分页对象DTO
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2017-11-14 09:11
 *
 */
@SuppressWarnings("all")
public class DataPage<T> implements Serializable {
  private static final long serialVersionUID = 1L;

  @Param(value = "当前页", required = true)
  private int pageNum;

  @Param(value = "每页的行数", required = true)
  private int pageSize;

  @Param(value = "总记录数", required = true)
  private long total;

  @Param(value = "总页数", required = true)
  private int pages;

  @Param(value = "结果集对象列表", required = true)
  private List<T> list;


  public DataPage(){
  }

  public DataPage(Page<T> page){
    this.pageNum = page.getNumber() < 0 ? 1 : page.getNumber() + 1;
    this.pageSize = page.getSize();
    this.total = page.getTotalElements();
    this.pages = page.getTotalPages() < 0 ? 1 : page.getTotalPages();
    this.list = page.getContent();
  }
//  public DataPage(PageInfo<T> pageInfo){
//    this.pageNum = pageInfo.getPageNum();
//    this.pageSize = pageInfo.getSize();
//    this.total = pageInfo.getTotal();
//    this.pages = pageInfo.getPages();
//    this.list = pageInfo.getList();
//  }

  public int getPageNum() {
    return pageNum;
  }

  public void setPageNum(int pageNum) {
    this.pageNum = pageNum;
  }

  public int getPageSize() {
    return pageSize;
  }

  public void setPageSize(int pageSize) {
    this.pageSize = pageSize;
  }

  public long getTotal() {
    return total;
  }

  public void setTotal(long total) {
    this.total = total;
  }

  public int getPages() {
    return pages;
  }

  public void setPages(int pages) {
    this.pages = pages;
  }

  public List<T> getList() {
    return list;
  }

  public void setList(List<T> list) {
    this.list = list;
  }

  public <X> DataPage<X> transform(List<X> data){
    return convert(this, data);
  }

  public static <X> DataPage<X> convert(DataPage page, List<X> data){
    page.setList(data);
    return page;
  }

  public static <T> DataPage<T> from(Page<T> page){
    return new DataPage<>(page);
  }

  public DataPage<T> map(Function<T, T> mapper) {
    if(!this.getList().isEmpty()){
      List<T> collect = this.getList().stream().map(mapper).collect(Collectors.toList());
      this.setList(collect);
    }
    return this;
  }

//  public static <T> DataPage<T> from(PageInfo<T> pageInfo){
//    return new DataPage<>(pageInfo);
//  }











  public static <T> Page<T> getEmptyPage(){
    return emptyPage;
  }


  public static <T> DataPage<T> getEmpty(){
    return empty;
  }

  private static final DataPage empty = new EmptyDataPage<>();

  private static class EmptyDataPage<T> extends DataPage<T> {
    private static final long serialVersionUID = 1L;
    private int pageNum;
    private int pageSize = Pages.PAGE_SIZE;
    private long total;
    private int pages;
    private List<T> list =  Collections.unmodifiableList(new ArrayList<>());

    @Override
    public int getPageNum() {
      return pageNum;
    }

    @Override
    public void setPageNum(int pageNum) {
      throw new UnsupportedOperationException();
    }

    @Override
    public int getPageSize() {
      return pageSize;
    }

    @Override
    public void setPageSize(int pageSize) {
      throw new UnsupportedOperationException();
    }

    @Override
    public long getTotal() {
      return total;
    }

    @Override
    public void setTotal(long total) {
      throw new UnsupportedOperationException();
    }

    @Override
    public int getPages() {
      return pages;
    }

    @Override
    public void setPages(int pages) {
      throw new UnsupportedOperationException();
    }

    @Override
    public List<T> getList() {
      return list;
    }

    @Override
    public void setList(List<T> list) {
      throw new UnsupportedOperationException();
    }
  }

  private static final Page emptyPage = new Page() {
    List content = Collections.unmodifiableList(new ArrayList<>());
    @Override
    public int getTotalPages() {
      return 0;
    }

    @Override
    public long getTotalElements() {
      return 0;
    }

    @Override
    public Page map(Function function) {
      return null;
    }

    @Override
    public int getNumber() {
      return 0;
    }

    @Override
    public int getSize() {
      return Pages.PAGE_SIZE;
    }

    @Override
    public int getNumberOfElements() {
      return 0;
    }

    @Override
    public List getContent() {
      return content;
    }

    @Override
    public boolean hasContent() {
      return false;
    }

    @Override
    public Sort getSort() {
      return null;
    }

    @Override
    public boolean isFirst() {
      return false;
    }

    @Override
    public boolean isLast() {
      return false;
    }

    @Override
    public boolean hasNext() {
      return false;
    }

    @Override
    public boolean hasPrevious() {
      return false;
    }

    @Override
    public Pageable nextPageable() {
      return null;
    }

    @Override
    public Pageable previousPageable() {
      return null;
    }

    @Override
    public Iterator iterator() {
      return null;
    }
  };
}
