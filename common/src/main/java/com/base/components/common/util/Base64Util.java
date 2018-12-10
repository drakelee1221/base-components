package com.base.components.common.util;

import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Base64Util
 *
 * @author <a href="lijian@xianyunsoft.com">Lijian</a>
 * @version 1.0.0, 2017-12-05
 */
public class Base64Util {
  public static String getBase64(InputStream fin) throws IOException {

    try(ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()){
      byte[] b = new byte[512];
      int i ;
      while ((i = fin.read(b)) !=-1){
        byteArrayOutputStream.write(b,0,i);
      }
      byteArrayOutputStream.flush();
      byte[] bytes = byteArrayOutputStream.toByteArray();
      return Base64.encodeBase64String(bytes);
    }
  }
}
