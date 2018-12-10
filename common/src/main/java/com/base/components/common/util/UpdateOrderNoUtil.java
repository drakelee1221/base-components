package com.base.components.common.util;

import com.base.components.common.exception.business.BusinessException;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * UpdateOrderNoUtil 更新排序号工具
 *
 * @author <a href="morse.jiang@foxmail.com">JiangWen</a>
 * @version 1.0.0, 2018/6/20 0020 10:04
 */
public class UpdateOrderNoUtil {


  /**
   * @param list 实体集合
   * @param insertEntity 待插入集合的实体
   * @param orderNoName 实体排序号对应的属性名
   * @param <T>
   *
   * @return
   */
  public static <T> List<T> execute(List<T> list, T insertEntity, String orderNoName) {
    Integer max = 0;
    if (list != null) {
      for (int i = 0; i < list.size(); i++) {
        Integer orderNo1 = getOrderNo(list.get(i), orderNoName);
        if (orderNo1 == null) {
          setOrderNo(list.get(i), orderNoName, max);
        }
        if (orderNo1 > max) {
          max = orderNo1;
        }
      }
      Collections.sort(list, new Comparator<T>() {
        @Override
        public int compare(T o1, T o2) {
          Integer orderNo1 = getOrderNo(o1, orderNoName);
          Integer orderNo2 = getOrderNo(o2, orderNoName);
          return orderNo1.compareTo(orderNo2);
        }
      });
      try {
        Integer orderNo = getOrderNo(insertEntity, orderNoName);
        if (orderNo == null || orderNo == 0) {
          setOrderNo(insertEntity, orderNoName, max);
        } else {
          for (int i = 0; i < list.size(); i++) {
            Integer orderNo2 = getOrderNo(list.get(i), orderNoName);
            if (orderNo2 >= orderNo) {
              setOrderNo(list.get(i), orderNoName, orderNo2 + 1);
            }
          }
        }
        list.add(insertEntity);
        Collections.sort(list, new Comparator<T>() {
          @Override
          public int compare(T o1, T o2) {
            Integer orderNo1 = getOrderNo(o1, orderNoName);
            Integer orderNo2 = getOrderNo(o2, orderNoName);
            return orderNo1.compareTo(orderNo2);
          }
        });
      } catch (Exception e) {
        e.printStackTrace();
        throw new BusinessException("更新排序号异常");
      }
      for (int i = 0; i < list.size(); i++) {
        setOrderNo(list.get(i), orderNoName, i + 1);
      }
    }
    return list;
  }

  private static <T> Integer getOrderNo(T insertEntity, String orderNoName) {
    try {
      if (insertEntity != null) {
        Field field = insertEntity.getClass().getDeclaredField(orderNoName);
        if (field != null) {
          field.setAccessible(true);
          Object object = field.get(insertEntity);
          if (object != null) {
            Integer orderNo = Integer.valueOf(field.get(insertEntity).toString());
            return orderNo;
          } else {
            return null;
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new BusinessException("更新排序号异常");
    }
    return null;
  }

  private static <T> void setOrderNo(T insertEntity, String orderNoName, int value) {
    try {
      if (insertEntity != null) {
        Field field = insertEntity.getClass().getDeclaredField(orderNoName);
        if (field != null) {
          field.setAccessible(true);
          field.set(insertEntity, value);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new BusinessException("更新排序号异常");
    }
  }

}
