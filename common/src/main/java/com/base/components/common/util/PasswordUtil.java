/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.common.util;



public class PasswordUtil {
  /**
   * 用户密码加密解密KEY
   */
  static final String PASSWORD_AES_KEY = "swrmArw81Y2uz3xLBPFEdg==";

  private static final String link = "\t";

  /**
   * 加密用户密码
   *
   * @param account
   * @param password
   *
   * @return
   *
   * @throws Exception
   */
  public static String encryptPassword(String account, String password) {
    StringBuilder sb = new StringBuilder();
    sb.append(account).append(link).append(password);
    try {
      return AESUtil.encrypt(sb.toString(), PASSWORD_AES_KEY);
    } catch (Exception e) {
      throw new IllegalArgumentException(e);
    }
  }

  /**
   * 解密用户密码
   *
   * @param account
   * @param encryptedPassword
   *
   * @return
   *
   * @throws Exception
   */
  public static String decryptPassword(String account, String encryptedPassword) {
    String linked = null;
    try {
      linked = AESUtil.decrypt(encryptedPassword, PASSWORD_AES_KEY);
    } catch (Exception e) {
      throw new IllegalArgumentException(e);
    }
    return linked.split("\\t")[1];
  }

  public static void main(String[] args) throws Exception {
    System.out.println(encryptPassword("15696209698", "123456"));
    System.out.println(decryptPassword("13112345678","8Kj9/BpetGZ0mYx0XndP+VfLLVNNoElO38YVGsx9U2g="));
  }
}
