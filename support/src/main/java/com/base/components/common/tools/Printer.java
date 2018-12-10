package com.base.components.common.tools;

/**
 * Printer
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-07-05 12:30
 */
public abstract class Printer {
  private static final String DEFAULT_END = "\033[0;39m";
  private static final String START = "\033[";
  private static final String END = "m";
  public static void info(Object o){
    print("94", o);
  }
  public static void err(Object o){
    print("31", o);
  }
  public static void warn(Object o){
    print("93", o);
  }
  public static void log(Object o){
    System.out.println(o);
  }
  private static void print(String color, Object src){
    System.out.println(START + color + END + String.valueOf(src) + DEFAULT_END);
  }
}
