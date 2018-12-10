package com.base.components.common.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.BitSet;

/**
 * @author <a href="tecyun@foxmail.com">Huangyunyang</a>
 * @version 1.0.0, 2018/4/12 0012 09:55
 */
public class URLUtil {

  private static BitSet chars = new BitSet(16);

  static {
    chars.set('!');
    chars.set('#');
    chars.set('$');
    chars.set('&');
    chars.set('\'');
    chars.set('(');
    chars.set(')');
    chars.set('*');
    chars.set('+');
    chars.set(',');
    chars.set(':');
    chars.set(';');
    chars.set('=');
    chars.set('?');
    chars.set('@');
    chars.set('[');
    chars.set(']');
    chars.set('~');
    chars.set('`');
    chars.set('%');
  }

  public static String encode(String srcUrl, char replaceChar) {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < srcUrl.length(); i++) {
      int c = (int) srcUrl.charAt(i);
      if (chars.get(c)) {
        sb.append(replaceChar);
      } else {
        sb.append((char) c);
      }
    }
    return sb.toString();
  }

  public static String getUri(String url){
    URL murl = null;
    try {
      murl = new URL(url);
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    return murl.getPath();
  }
}
