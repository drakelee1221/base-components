package com.base.components.common.util;

import org.springframework.util.Assert;

import java.util.Random;

public abstract class CaptchaUtil {
  /**
   * 随机产生的字符串
   */
  private static final String RANDOM_STRS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";

  /**
   * 对应的后台加密是：先生成一个32的随机字符串，再根据length循环生成code
   */
  public static final String jsDecrypt_1
    = "<script>eval(function(p,a,c,k,e,d){e=function(c){return(c<a?\"\":e(parseInt(c/a)))+((c=c%a)>35?String.fromCharCode(c+29):c.toString(36))};if(!''.replace(/^/,String)){while(c--)d[e(c)]=k[c]||e(c);k=[function(e){return d[e]}];e=function(){return'\\\\w+'};c=1;};while(c--)if(k[c])p=p.replace(new RegExp('\\\\b'+e(c)+'\\\\b','g'),k[c]);return p;}('7 8(3,4){6 5=\"\";9(6 2=0;2<3;2++){b(3*(2+1)>=4.d){4+=4}5+=4.a(3*(2+1),3*(2+1)+1)}c 5}',14,14,'||i|number|original|code|var|function|jsDecrypt|for|substring|if|return|length'.split('|'),0,{}))</script>";

  /**
   * 对应的后台加密是：先得到length长得code，在生成一个混淆的随机字符串
   */
  public static final String jsDecrypt_2
    = "<script>eval(function(p,a,c,k,e,d){e=function(c){return(c<a?\"\":e(parseInt(c/a)))+((c=c%a)>35?String.fromCharCode(c+29):c.toString(36))};if(!''.replace(/^/,String)){while(c--)d[e(c)]=k[c]||e(c);k=[function(e){return d[e]}];e=function(){return'\\\\w+'};c=1;};while(c--)if(k[c])p=p.replace(new RegExp('\\\\b'+e(c)+'\\\\b','g'),k[c]);return p;}('p n(o,4){3 d=4.9(4.a-2,4.a-1);3 c=4.9(4.a-1);3 6=[\"A\",\"B\",\"C\",\"D\",\"E\",\"F\",\"G\",\"H\",\"I\",\"J\",\"K\",\"L\",\"M\",\"r\",\"q\",\"h\",\"j\",\"l\",\"k\",\"m\",\"x\",\"w\",\"z\",\"y\",\"t\",\"s\"];3 5=0;3 7=0;v(3 i u 6){8(6[i]===d.b()){5=e(i)+1}8(6[i]===c.b()){7=e(i)+1}8(5>0&&7>0){3 g=4.9(5,5+7);f g}}f\"\"}',49,49,'|||var|original|firstNumber|saveLet|secondNumber|if|substring|length|toUpperCase|second|first|parseInt|return|res|P||Q|S|R|T|jsDecrypt|number|function|O|N|Z|Y|in|for|V|U|X|W|||||||||||||'.split('|'),0,{}))</script>";

  /**
   * 对应的后台加密是：先生成一个32的随机字符串，再根据length循环去除字符
   */
  public static final String jsDecrypt_3
    = "<script>eval(function(p,a,c,k,e,d){e=function(c){return(c<a?\"\":e(parseInt(c/a)))+((c=c%a)>35?String.fromCharCode(c+29):c.toString(36))};if(!''.replace(/^/,String)){while(c--)d[e(c)]=k[c]||e(c);k=[function(e){return d[e]}];e=function(){return'\\\\w+'};c=1;};while(c--)if(k[c])p=p.replace(new RegExp('\\\\b'+e(c)+'\\\\b','g'),k[c]);return p;}('9 a(2,0){8{6 3=0.7(2,2+1);0=0.f(e b(3,\"c\"),\"\");4(0.5<2){0=3+0}}4(0.5>2);d 0}',16,16,'original||number|tmp|while|length|var|substring|do|function|jsDecrypt|RegExp|gm|return|new|replace'.split('|'),0,{}))</script>";
  private static final ThreadLocal<Random> random = new ThreadLocal<Random>() {
    @Override
    protected Random initialValue() {
      return new Random();
    }
  };


  /**
   * 产生随机字符
   *
   * @param length 字符长度
   *
   * @return
   */
  public static String getRandomCode(int length) {
    Assert.isTrue(length > 0, "randomCode长度须大于零");
    StringBuffer code = new StringBuffer();
    for (int i = 0; i < length; i++) {
      code.append(getRandomString(random.get().nextInt(RANDOM_STRS.length())));
    }
    return code.toString();
  }


  /**
   * 混淆处理随机字符
   *
   * @param randomCode 不能为空且长度须在四到十位以内
   *
   * @return 还原规则：
   * 取返回值的倒数第二位字母对应的字母表顺序数（从1开始）作为截取开始下标，倒数第一位字母对应的字母表顺序数（从1开始）作为截取长度。
   * 如：G9VLKW4N6M225FT6V5RXAC8FMEDCYKE39ZCD，最后两位CD对应的顺序数（从1开始）是34，那么截取规则：
   * "G9VLKW4N6M225FT6V5RXAC8FMEDCYKE39ZCD".substring(3,3+4) = "LKW4"
   */
  public static String getEncryptRandomCode(String randomCode) {
    Assert.isTrue(randomCode != null && randomCode.length() < 10, "randomCode不能为空且长度须在十位以内");
    int begin = random.get().nextInt(10) + 1;
    String encryptRandomCode = getRandomCode(begin) + randomCode + getRandomCode(30 - begin) + rules[begin - 1] + rules[
      randomCode.length() - 1];
    return encryptRandomCode;
  }

  private static final String[] rules = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P",
    "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};



  /**
   * 根据随机字符串获取一个code
   * 规则：
   * 根据传入的随机字符串先取index为length的字符，接着再取往下取index为2*length位的字符，循环取到length个字符后组成新的字符串即为code
   * 比如
   * original="3ZRKBMV8956QYMWRNRTRZR2HCFH5U9QA"
   * length = 4
   * 那么：code = "B9YN"（original.substring(4,5) + original.substring(8,9) + ......）
   *
   * @param original
   * @param length
   *
   * @return
   */
  public static String getCode(String original, Integer length) {
    String code = "";
    //根据number循环数number位组成验证码
    for (int i = 0; i < length; i++) {
      if (length * (i + 1) >= original.length()) {
        original += original;
      }
      code += original.substring(length * (i + 1), length * (i + 1) + 1);
    }
    return code;
  }


  /**
   * 生成简单code
   * 根据length循环去除字符
   *
   * @return
   */
  public static String getSimpleCode(String original, Integer length) {
    do {
      String tmp = original.substring(length, length + 1);
      original = original.replace(tmp, "");
      while (original.length() < length) {
        //字符串添加tmp
        original = String.format(tmp + "%s", original);
      }
    } while (original.length() > length);
    return original;
  }



  /**
   * 获取随机的字符
   */
  private static String getRandomString(int num) {
    return String.valueOf(RANDOM_STRS.charAt(num));
  }

  public static void main(String[] args) {
    //    String c = getRandomCode(4);
    //    String ec = getEncryptRandomCode(c);
    //
    //    System.out.println(c);
    //    System.out.println(ec);
    //    System.out.println("JG99LPGUURSE24ZQNPVFJHLKLA3CJXUSBYY6YPCH".substring(3, 3 + 8));
    String t = getSimpleCode("3ZRKBMV8956QYMWRNRTRZR2HCFH5U9QA", 5);
    System.out.println(t);
  }
}