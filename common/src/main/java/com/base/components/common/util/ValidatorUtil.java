/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.common.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator;

import java.util.regex.Pattern;

/**
 * 合法性校验工具类
 *
 * @author <a href="lijian@xianyunsoft.com">Lijian</a>
 * @version 1.0.0, 2017-08-11
 */
public class ValidatorUtil {

  /**
   * 正则表达式：验证手机号
   */
 private static final String REGEX_MOBILE = "^[1][3-9]\\d{9}$";

  /**
   * 正则表达式：验证身份证
   */
  private static final String REGEX_ID_CARD = "(^\\d{18}$)|(^\\d{15}$)";


  /**
   * 校验手机号
   *
   * @param phoneNum
   *
   * @return 校验通过返回true，否则返回false
   */
  public static boolean isPhoneNum(String phoneNum) {
    return Pattern.matches(REGEX_MOBILE, phoneNum);
  }

  /**
   * 校验邮箱
   *
   * @param email
   *
   * @return 校验通过返回true，否则返回false
   */
  public static boolean isEmail(String email) {
    EmailValidator emailValidator = new EmailValidator();
    if (StringUtils.isBlank(email)) {
      return false;
    }
    return emailValidator.isValid(email, null);
  }


  /**
   * 校验身份证
   *
   * @param idCard
   *
   * @return 校验通过返回true，否则返回false
   */
  public static boolean isIDCard(String idCard) {
    return Pattern.matches(REGEX_ID_CARD, idCard);
  }

  public static void main(String[] args) {
    System.out.println(NumberUtils.isDigits("123213241432153124315321341431"));
    System.out.println(new EmailValidator().isValid("sean3112@vi.p.com.cn", null));
    /*String s="0.1";
    System.out.println(isPositiveInteger(s));*/
  }
}
