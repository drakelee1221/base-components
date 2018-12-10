package com.base.components.common.util;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.Set;

/**
 * SetsHelper
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-07-06 14:07
 */
public abstract class SetsHelper {

  /**
   * 比较两个Set中是否都含有同一个元素
   * @param s1
   * @param s2
   *
   * @return
   */
  public static <E> boolean existsSameElement(Set<E> s1, Set<E> s2){
    if(s1 != null && s2 != null && !s1.isEmpty() && !s2.isEmpty()){
      if(s1.size() < s2.size()){
        for (E o : s1) {
          if(s2.contains(o)){
            return true;
          }
        }
      }
      else{
        for (E o : s2) {
          if(s1.contains(o)){
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * 把字符串分割后，转换为Set
   * @param srcString
   * @param separatorChars
   *
   * @return
   */
  public static Set<String> toStringSets(String srcString, String separatorChars) {
    Set<String> tags = Collections.emptySet();
    if (StringUtils.isNotBlank(srcString)) {
      tags = Sets.newHashSet(StringUtils.split(srcString, separatorChars));
    }
    return tags;
  }

}
