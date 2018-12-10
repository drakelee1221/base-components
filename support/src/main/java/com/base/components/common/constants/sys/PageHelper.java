package com.base.components.common.constants.sys;

import com.base.components.common.dto.page.MpPage;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static com.base.components.common.constants.sys.Pages.*;

/**
 * PageHelper
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-09-12 14:38
 */
public class PageHelper {

  public static final PageHelper INSTANCE = new PageHelper();

  PageHelper(){}

  /**
   * 转换排序方向
   *
   * @param directionStr - 转换源字符串
   *
   * @return asc / desc
   */
  public Sort.Direction sortDirection(String directionStr) {
    if (Sort.Direction.DESC.toString().equalsIgnoreCase(directionStr)) {
      return Sort.Direction.DESC;
    } else {
      return Sort.Direction.ASC;
    }
  }


  /**
   * 返回大于 0 等当前页数
   *
   * @param pageNum 参数传入的当前页数
   */
  public int pageNum(int pageNum) {
    if (pageNum < PAGE_NUM) {
      return PAGE_NUM;
    }
    return pageNum;
  }

  /**
   * 返回大于 0 等当前页数
   *
   * @param pageNum 参数传入的当前页数
   */
  public int pageNum(String pageNum) {
    int srcVal = convert(pageNum, PAGE_NUM);
    return pageNum(srcVal);
  }

  /**
   * 返回不大于{@link Pages#PAGE_SIZE}的每页行数
   *
   * @param pageSize 参数传入的每页行数
   */
  public int pageSize(int pageSize) {
    return pageSize(pageSize, PAGE_SIZE);
  }

  /**
   * 返回不大于{@link Pages#PAGE_SIZE}的每页行数
   *
   * @param pageSize 参数传入的每页行数
   */
  public int pageSize(String pageSize) {
    return pageSize(pageSize, PAGE_SIZE);
  }

  /**
   * 返回不大于max的每页行数
   *
   * @param pageSize 参数传入的每页行数
   * @param max 最大每页行数，为空时取{@link Pages#PAGE_SIZE}
   */
  public int pageSize(int pageSize, Integer max) {
    if (max != null && pageSize > max) {
      return max;
    }
    if (pageSize <= 0) {
      return PAGE_SIZE;
    }
    return pageSize;
  }

  /**
   * 返回不大于max的每页行数
   *
   * @param pageSize 参数传入的每页行数
   * @param max 最大每页行数，为空时取{@link Pages#PAGE_SIZE}
   */
  public int pageSize(String pageSize, Integer max) {
    if (max == null || pageSize == null) {
      max = PAGE_SIZE;
    }
    int srcVal = convert(pageSize, max);
    return pageSize(srcVal, max);
  }


  /**
   * 得到分页接口
   * request.getParameter("pageNum") = 当前页数（默认第1页） <br/>
   * request.getParameter("pageCount") = 每页行数（默认 {@link Pages#PAGE_SIZE} 行）
   *
   * @param param -
   * <p> pageNum         - Nullable - Int - 当前页数
   * <p> pageSize        - Nullable - Int - 每页行数
   * @param sort
   *
   * @return
   */
  public Pageable pageable(Map<String, String> param, Sort sort) {
    return pageable(pageNum(param.get(PAGE_NUM_VAR_NAME)), pageSize(param.get(PAGE_SIZE_VAR_NAME)), sort, null);
  }

  /**
   * 得到分页接口
   * request.getParameter("pageNum") = 当前页数（默认第1页） <br/>
   * request.getParameter("pageCount") = 每页行数（默认 {@link Pages#PAGE_SIZE} 行）
   *
   * @param param -
   * <p> pageNum         - Nullable - Int - 当前页数
   * <p> pageSize        - Nullable - Int - 每页行数
   * @param maxPageSize
   * @param sort
   *
   * @return
   */
  public Pageable pageable(Map<String, String> param, Integer maxPageSize, Sort sort) {
    return pageable(
      pageNum(param.get(PAGE_NUM_VAR_NAME)), pageSize(param.get(PAGE_SIZE_VAR_NAME), maxPageSize), sort, null);
  }


  /**
   * 得到分页接口
   * request.getParameter("pageNum") = 当前页数（默认第1页） <br/>
   * request.getParameter("pageCount") = 每页行数（默认 {@link Pages#PAGE_SIZE} 行）
   *
   * @param request
   * @param sort
   * @param maxPageSize
   *
   * @return
   */
  public Pageable pageable(HttpServletRequest request, Sort sort, Integer maxPageSize) {
    int page = pageNum(request.getParameter(PAGE_NUM_VAR_NAME));
    int rows = pageSize(request.getParameter(PAGE_SIZE_VAR_NAME), maxPageSize);
    if (sort == null) {
      return PageRequest.of(page - 1, rows);
    } else {
      return PageRequest.of(page - 1, rows, sort);
    }
  }

  /**
   * 得到分页接口
   * request.getParameter("pageNum") = 当前页数（默认第1页） <br/>
   * request.getParameter("pageCount") = 每页行数（默认 {@link Pages#PAGE_SIZE} 行）
   *
   * @param request
   * @param sort
   *
   * @return
   */
  public Pageable pageable(HttpServletRequest request, Sort sort) {
    return pageable(request, sort, null);
  }

  /**
   * 得到分页接口
   *
   * @param pageNum 当前页数（默认第1页）
   * @param pageSize 每页行数（默认{@link Pages#PAGE_SIZE}行）
   * @param sort
   * @param maxPageSize
   *
   * @return
   */
  public Pageable pageable(int pageNum, int pageSize, Sort sort, Integer maxPageSize) {
    int page = pageNum(pageNum);
    int rows = pageSize(pageSize, maxPageSize);
    if (sort == null) {
      return PageRequest.of(page - 1, rows);
    } else {
      return PageRequest.of(page - 1, rows, sort);
    }
  }

  /**
   * 得到分页接口, 默认每页
   *
   * @param pageNum 当前页数（默认第1页）
   * @param pageSize 每页行数（默认{@link Pages#PAGE_SIZE}行）
   * @param sort
   *
   * @return
   */
  public Pageable pageable(int pageNum, int pageSize, Sort sort) {
    return pageable(pageNum, pageSize, sort, null);
  }

  /**
   * mybatis分页接口
   *
   * @param pageNum
   * @param pageSize
   *
   * @return
   */
  public <T> MpPage<T> mybatisPage(int pageNum, int pageSize) {
    return new MpPage<>(pageNum, pageSize);
  }

  /**
   * mybatis分页接口
   *
   * @param pageNum
   * @param pageSize
   * @param orderByField
   *
   * @return
   */
  public <T> MpPage<T> mybatisPage(int pageNum, int pageSize, String orderByField) {
    return new MpPage<>(pageNum, pageSize, orderByField);
  }


  /**
   * mybatis分页接口
   *
   * @param pageNum
   * @param pageSize
   * @param orderByField
   * @param isAsc
   *
   * @return
   */
  public <T> MpPage<T> mybatisPage(int pageNum, int pageSize, String orderByField, boolean isAsc) {
    return new MpPage<>(pageNum, pageSize, orderByField, isAsc);
  }

  public <T> MpPage<T> mybatisPage(Map<String, String> param) {
    return new MpPage<>(pageNum(param.get(PAGE_NUM_VAR_NAME)), pageSize(param.get(PAGE_SIZE_VAR_NAME)));
  }

  public <T> MpPage<T> mybatisPage(Map<String, String> param, String orderByField) {
    return new MpPage<>(pageNum(param.get(PAGE_NUM_VAR_NAME)), pageSize(param.get(PAGE_SIZE_VAR_NAME)), orderByField);
  }

  public <T> MpPage<T> mybatisPage(Map<String, String> param, String orderByField, boolean isAsc) {
    return new MpPage<>(pageNum(param.get(PAGE_NUM_VAR_NAME)), pageSize(param.get(PAGE_SIZE_VAR_NAME)), orderByField,
                        isAsc
    );
  }


  protected int convert(String src, int defaultValue) {
    try {
      return Integer.parseInt(src);
    } catch (Exception e) {
      return defaultValue;
    }
  }
}
