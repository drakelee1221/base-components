/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.elasticsearch.repository.base;

import com.base.components.common.constants.InvasiveRewrite;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.FacetedPage;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.facet.FacetResult;

import java.util.List;

/**
 * NoneFacetedAggregatedPage
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-02-13 16:05
 */
@InvasiveRewrite
public class NoneFacetedAggregatedPage<T> extends PageImpl<T> implements FacetedPage<T>, AggregatedPage<T> {

  public NoneFacetedAggregatedPage(List<T> content, Pageable pageable, long total) {
    super(content, pageable, total);
  }

  public NoneFacetedAggregatedPage(List<T> content) {
    super(content);
  }

  @Override
  public boolean hasAggregations() {
    return false;
  }

  @Override
  public Aggregations getAggregations() {
    return null;
  }

  @Override
  public Aggregation getAggregation(String name) {
    return null;
  }

  @Override
  public boolean hasFacets() {
    return false;
  }

  @Override
  public List<FacetResult> getFacets() {
    return null;
  }

  @Override
  public FacetResult getFacet(String name) {
    return null;
  }

  @Override
  public String getScrollId() {
    return null;
  }
}
