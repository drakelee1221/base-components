/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.common.util;

import com.google.common.net.HttpHeaders;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;

/**
 * @author <a href="huangchaofei@xianyunsoft.com">Huang Chaofei</a>
 * @version 1.0.0, 2017-07-25
 */
public class IPUtils {
  public static final String LOCAL_IPV4 = "127.0.0.1";
  public static final String LOCAL_IPV6 = "0:0:0:0:0:0:0:1";
  public static final String LOCAL_HOST = "localhost";
  public static final String UNKNOWN = "unknown";


  public static String getRealIp(HttpServletRequest request) {
    String ip = request.getHeader(HttpHeaders.X_FORWARDED_FOR);
    if (null == ip || 0 == ip.length() || UNKNOWN.equalsIgnoreCase(ip)) {
      ip = request.getHeader("Proxy-Client-IP");
    }
    if (null == ip || 0 == ip.length() || UNKNOWN.equalsIgnoreCase(ip)) {
      ip = request.getHeader("WL-Proxy-Client-IP");
    }
    if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
      ip = request.getHeader("X-Real-IP");
    }
    if (null == ip || 0 == ip.length() || UNKNOWN.equalsIgnoreCase(ip)) {
      ip = request.getRemoteAddr();
    }
    if(LOCAL_IPV6.equals(ip)){
      return LOCAL_IPV4;
    }
    if(ip != null && ip.contains(",")){
      String[] arr = StringUtils.split(ip, ",");
      if(arr.length > 0){
        return arr[0];
      }
    }
    return ip;
  }

  public static String getLocalMACAddress() {
    try {
      return getMACAddress(InetAddress.getLocalHost());
    } catch (Exception e) {
      return null;
    }
  }

  public static String getMACAddress(String host) {
    try {
      return getMACAddress(InetAddress.getByName(host));
    } catch (Exception e) {
      try {
        UdpGetClient client = new UdpGetClient(host);
        return client.getRemoteMacAddr();
      } catch (Exception ignored) {
      }
    }
    return null;
  }

  private static String getMACAddress(InetAddress ia) throws Exception {
    //获得网络接口对象（即网卡），并得到mac地址，mac地址存在于一个byte数组中。
    byte[] mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();

    //下面代码是把mac地址拼装成String
    StringBuffer sb = new StringBuffer();

    for (int i = 0; i < mac.length; i++) {
      if (i != 0) {
        sb.append("-");
      }
      //mac[i] & 0xFF 是为了把byte转化为正整数
      String s = Integer.toHexString(mac[i] & 0xFF);
      sb.append(s.length() == 1 ? 0 + s : s);
    }

    //把字符串所有小写字母改为大写成为正规的mac地址并返回
    return sb.toString().toUpperCase();
  }



  private static class UdpGetClient {
    private String sRemoteAddr;
    private int iRemotePort = 137;
    private byte[] buffer = new byte[1024];
    private DatagramSocket ds = null;

    public UdpGetClient(String addr) throws Exception {
      sRemoteAddr = addr;
      ds = new DatagramSocket();
    }

    protected final DatagramPacket send(final byte[] bytes) throws IOException {
      DatagramPacket dp = new DatagramPacket(bytes, bytes.length, InetAddress.getByName(sRemoteAddr), iRemotePort);
      ds.send(dp);
      return dp;
    }

    final DatagramPacket receive() throws Exception {
      DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
      ds.setSoTimeout(5000);
      ds.receive(dp);
      return dp;
    }

    // 询问包结构:
    // Transaction ID 两字节（16位） 0x00 0x00
    // Flags 两字节（16位） 0x00 0x10
    // Questions 两字节（16位） 0x00 0x01
    // AnswerRRs 两字节（16位） 0x00 0x00
    // AuthorityRRs 两字节（16位） 0x00 0x00
    // AdditionalRRs 两字节（16位） 0x00 0x00
    // Name:array [1..34] 0x20 0x43 0x4B 0x41(30个) 0x00 ;
    // Type:NBSTAT 两字节 0x00 0x21
    // Class:INET 两字节（16位）0x00 0x01
    byte[] getQueryCmd() throws Exception {
      byte[] t_ns = new byte[50];
      t_ns[0] = 0x00;
      t_ns[1] = 0x00;
      t_ns[2] = 0x00;
      t_ns[3] = 0x10;
      t_ns[4] = 0x00;
      t_ns[5] = 0x01;
      t_ns[6] = 0x00;
      t_ns[7] = 0x00;
      t_ns[8] = 0x00;
      t_ns[9] = 0x00;
      t_ns[10] = 0x00;
      t_ns[11] = 0x00;
      t_ns[12] = 0x20;
      t_ns[13] = 0x43;
      t_ns[14] = 0x4B;

      for (int i = 15; i < 45; i++) {
        t_ns[i] = 0x41;
      }

      t_ns[45] = 0x00;
      t_ns[46] = 0x00;
      t_ns[47] = 0x21;
      t_ns[48] = 0x00;
      t_ns[49] = 0x01;
      return t_ns;
    }

    // 表1 “UDP－NetBIOS－NS”应答包的结构及主要字段一览表
    // 序号 字段名 长度
    // 1 Transaction ID 两字节（16位）
    // 2 Flags 两字节（16位）
    // 3 Questions 两字节（16位）
    // 4 AnswerRRs 两字节（16位）
    // 5 AuthorityRRs 两字节（16位）
    // 6 AdditionalRRs 两字节（16位）
    // 7 Name<Workstation/Redirector> 34字节（272位）
    // 8 Type:NBSTAT 两字节（16位）
    // 9 Class:INET 两字节（16位）
    // 10 Time To Live 四字节（32位）
    // 11 Length 两字节（16位）
    // 12 Number of name 一个字节（8位）
    // NetBIOS Name Info 18×Number Of Name字节
    // Unit ID 6字节（48位
    final String getMacAddr(byte[] brevdata) throws Exception {
      // 获取计算机名
      int i = brevdata[56] * 18 + 56;
      String sAddr = "";
      StringBuffer sb = new StringBuffer(17);
      // 先从第56字节位置，读出Number Of Names（NetBIOS名字的个数，其中每个NetBIOS Names Info部分占18个字节）
      // 然后可计算出“Unit ID”字段的位置＝56＋Number Of Names×18，最后从该位置起连续读取6个字节，就是目的主机的MAC地址。
      for (int j = 1; j < 7; j++) {
        sAddr = Integer.toHexString(0xFF & brevdata[i + j]);
        if (sAddr.length() < 2) {
          sb.append(0);
        }
        sb.append(sAddr.toUpperCase());
        if (j < 6) {
          sb.append('-');
        }
      }
      return sb.toString();
    }

    final void close() {
      try {
        ds.close();
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }

    final String getRemoteMacAddr() throws Exception {
      String smac;
      try {
        byte[] bqcmd = getQueryCmd();
        send(bqcmd);
        DatagramPacket dp = receive();
        smac = getMacAddr(dp.getData());
      } finally {
        close();
      }
      return smac;
    }
  }
}
