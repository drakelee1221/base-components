package com.base.components.common.exception.business;

/**
 * SmsException
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-03-16 14:57
 */
public class SmsException extends BusinessException{
  private static final long serialVersionUID = 1L;

  public SmsException(ErrorCode errorCode, Throwable cause){
    super(errorCode.info, cause, errorCode.code);
  }

  public SmsException(String message, Integer errorCode){
    super(message, errorCode);
  }

  public SmsException(ErrorCode errorCode){
    super(errorCode.info, errorCode.code);
  }

  public static void assertIsTrue(boolean expression, ErrorCode errorCode){
    if(!expression){
      throw new SmsException(errorCode);
    }
  }

  public static void assertIsTrue(boolean expression, int errorCode, String meassge){
    if(!expression){
      throw new SmsException(meassge, errorCode);
    }
  }

  public enum ErrorCode{
    type_empty(100, "发送类型为空"),
    type_error(101, "发送类型错误"),
    validate_empty(102, "校验验证码为空"),
    validate_error(103, "校验验证码错误"),
    phone_empty(104, "手机号为空"),
    phone_error(105, "手机格式不正确"),
    error_user(106, "未找到用户"),
    phone_has_register(107, "该手机已注册"),
    count_limit(108, "此手机号获取短信验证码超过当天限制:{count}次，请明日再试"),
    encrypt_code_type(109, "获取混淆code的长度为数字且不能为空"),
    encrypt_code_limit(110, "获取混淆code的长度不合法"),
    password_error_limit(111, "密码连续{count}次错误"),
    request_frequently(112, "请求过于频繁"),
    js_null(113,"js解密码为空"),
    js_expire(114,"js解密码已过期，请重新获取"),
    order_code_null(115,"编号为空"),
    ;

    private int code;
    private String info;

    ErrorCode(int code, String info) {
      this.code = code;
      this.info = info;
    }

    public int getCode() {
      return code;
    }

    public String getInfo() {
      return info;
    }
  }
}
