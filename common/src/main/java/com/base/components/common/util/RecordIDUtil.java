package com.base.components.common.util;

import com.base.components.common.constants.sys.Dates;
import org.joda.time.DateTime;

import java.util.Random;

/**
 * 流水号util
 *
 * @author <a href="tecyun@foxmail.com">Huangyunyang</a>
 * @version 1.0.0, 2018/3/19 0019 17:26
 */
public class RecordIDUtil {

  public static String generateTransRecordId(){
    StringBuffer sb = new StringBuffer();
    DateTime now = DateTime.now();
    sb.append(now.toString(Dates.DATE_RECORD_FORMATTER_PATTERN))
      .append(new Random().nextInt(999999));
    return sb.toString();
  }
}
