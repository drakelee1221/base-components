package com.base.components.common.constants;

/**
 * 是否有效/启用/可用
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2017-07-08 14:01
 */
public enum Valid{
  TRUE(1),
  FALSE(0);

  private int val;

  public int getVal() {
    return val;
  }

  public boolean toBool(){
    return val == 1;
  }

  Valid(int val) {
    this.val = val;
  }

  public static Valid parseVal(int val){
    if(val == 1) {
      return TRUE;
    }
    if(val == 0) {
      return FALSE;
    }
    return null;
  }


  public static String toBoolDocDesc(){
    return "true=是、false=否";
  }

  public static String toIntDocDesc(){
    return "1=是、0=否";
  }

}
