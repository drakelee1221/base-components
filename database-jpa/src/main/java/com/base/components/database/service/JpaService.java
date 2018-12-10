/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.database.service;

import com.base.components.common.dto.page.DataPage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.Query;
import java.io.Serializable;
import java.util.List;

/**
 * JpaService
 *
 * @param <T> 实体类
 * @param <ID> 实体类ID
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-02-13 10:21
 */
interface JpaService<T extends Serializable, ID extends Serializable> {

  /**
   * 查询该表所有实体对象列表
   *
   * @return List
   */
  List<T> findAll();

  /**
   * 排序查询该表所有实体对象列表
   *
   * @return List
   */
  List<T> findAll(Sort paramSort);

  /**
   * 根据ID集合查询该表所有实体对象列表
   *
   * @param paramIterable ID集合
   *
   * @return List
   */
  List<T> findAllById(Iterable<ID> paramIterable);

  /**
   * 根据分页对象，查询分页
   *
   * @param paramPageable 分页对象
   *
   * @return Page
   */
  Page<T> findAll(Pageable paramPageable);

  /**
   * 根据条件查询实体对象列表
   *
   * @param paramSpecification 查询条件
   *
   * @return List
   */
  List<T> findAll(Specification<T> paramSpecification);

  /**
   * 根据条件和分页对象，查询分页
   *
   * @param paramSpecification 查询条件
   * @param paramPageable 分页对象
   *
   * @return Page
   */
  Page<T> findAll(Specification<T> paramSpecification, Pageable paramPageable);

  /**
   * 根据条件和排序对象，查询列表
   *
   * @param paramSpecification 查询条件
   * @param paramSort 排序对象
   *
   * @return List
   */
  List<T> findAll(Specification<T> paramSpecification, Sort paramSort);

  /**
   * 查询总数
   *
   * @return long
   */
  long count();

  /**
   * 根据条件查询总数
   *
   * @param paramSpecification 查询条件
   *
   * @return long
   */
  long count(Specification<T> paramSpecification);

  /**
   * 根据ID返回一个实体对象的引用，如果为空，则抛出异常
   *
   * @param paramID ID
   *
   * @return T
   */
  T getOne(ID paramID);

  /**
   * 根据ID返回一个实体对象的引用，可能为空
   *
   * @param paramID ID
   *
   * @return T
   */
  T findById(ID paramID);

  /**
   * 根据条件查询一个实体对象，如果多余一个，则抛出异常
   *
   * @param paramSpecification 查询条件
   *
   * @return
   */
  T findOne(Specification<T> paramSpecification);

  /**
   * 根据ID判断是否存在于数据集
   *
   * @param paramID ID
   *
   * @return boolean
   */
  boolean existsById(ID paramID);


  /**
   * 设置为每行转换为Map的返回类型
   *
   * @param query query接口
   *
   * @return Query
   */
  Query setMapResult(Query query);

  /**
   * 分页
   *
   * @param countQuery 总数查询接口
   * @param listQuery 列表查询接口
   * @param pageNum 当前第几页
   * @param pageSize 每页行数
   *
   * @return DataPage
   */
  DataPage<T> pageByQuery(Query countQuery, Query listQuery, Integer pageNum, Integer pageSize);

}
