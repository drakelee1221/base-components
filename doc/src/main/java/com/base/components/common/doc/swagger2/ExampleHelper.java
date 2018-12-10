package com.base.components.common.doc.swagger2;

import com.base.components.common.constants.sys.Dates;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.Date;

import static com.base.components.common.doc.DocConstants.DECIMAL_POINT;

/**
 * ExampleHelper
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-08-25 16:04
 */
public abstract class ExampleHelper {

  public static Object parseExample(Class<?> type, String example) {
    if (StringUtils.isNotBlank(example)) {
      try {
        if (Number.class.isAssignableFrom(type)) {
          if (example.contains(DECIMAL_POINT) || Double.class.isAssignableFrom(type) || Float.class.isAssignableFrom(type)
            || BigDecimal.class.isAssignableFrom(type)) {
            return new BigDecimal(example);
          } else {
            return Long.valueOf(example);
          }
        } else if (Boolean.class.equals(type)) {
          return Boolean.valueOf(example);
        }
        return example;
      } catch (Exception ignore) {
      }
    }
    else if(Date.class.isAssignableFrom(type)){
      return Dates.DATE_TIME_DOC_EXP;
    }
    else if(Boolean.class.equals(type)){
      return Boolean.TRUE;
    }
    return null;
  }
}
