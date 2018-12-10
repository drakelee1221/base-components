/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.common.util;

import net.ipip.datx.IPv4FormatException;
import net.ipip.datx.Util;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

/**
 * IpLocationUtil
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-05-10 13:42
 */
public class IpLocationUtil {

  private static final AtomicReference<byte[]> IP_DATABASE = new AtomicReference<>();
  /**
   * ip库地址, 更新版本 20180312
   * 见 https://www.ipip.net/download.html
   */
  private static byte[] getIpDatabase() throws IOException {
    if(IP_DATABASE.get() == null){
      try(InputStream inputStream = IpLocationUtil.class.getClassLoader().getResourceAsStream("ip-db_20180312.datx")) {
        Assert.notNull(inputStream, "未找到IP库文件");
        byte[] bytes = StreamUtils.copyToByteArray(inputStream);
        IP_DATABASE.set(bytes);
      }
    }
    return IP_DATABASE.get();
  }

  /**
   * 查询IP位置
   * @param ipv4 - ipv4
   *
   * @return [国家, 省, 市, 区县（一般空字符串）]
   */
  public static String[] find(String ipv4){
    try {
      byte[] database = getIpDatabase();
      return find(ipv4, database, Util.bytesToLong(database[0], database[1], database[2], database[3]));
    } catch (IPv4FormatException e) {
      throw new RuntimeException(ipv4 + " 不是IPV4", e);
    } catch (Exception e) {
      throw new RuntimeException(ipv4 + " 解析出错", e);
    }
  }

  private static String[] find(String ips, byte[] data, long indexSize) throws IPv4FormatException {

    if (!Util.isIPv4Address(ips)) {
      throw new IPv4FormatException();
    }

    long val = Util.ip2long(ips);
    int start = 262148;
    int low = 0;
    int mid = 0;
    int high = new Long((indexSize - 262144 - 262148) / 9).intValue() - 1;
    int pos = 0;
    while (low <= high)
    {
      mid = new Double((low + high) / 2).intValue();
      pos = mid * 9;

      long s = 0;
      if (mid > 0)
      {
        int pos1 = (mid - 1) * 9;
        s = Util.bytesToLong(data[start + pos1], data[start + pos1+1], data[start + pos1+2], data[start + pos1+3]);
      }

      long end = Util.bytesToLong(data[start + pos], data[start + pos+1], data[start + pos+2], data[start + pos+3]);
      if (val > end) {
        low = mid + 1;
      } else if (val < s) {
        high = mid - 1;
      } else {

        byte b =0;
        long off = Util.bytesToLong(b, data[start+pos+6],data[start+pos+5],data[start+pos+4]);
        long len = Util.bytesToLong(b, b, data[start+pos+7], data[start+pos+8]);

        int offset = new Long(off - 262144 + indexSize).intValue();

        byte[] loc = Arrays.copyOfRange(data, offset, offset+new Long(len).intValue());

        return new String(loc, Charset.forName("UTF-8")).split("\t", -1);
      }
    }

    return null;
  }
}
