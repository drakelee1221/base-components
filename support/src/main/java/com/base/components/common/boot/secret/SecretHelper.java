//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.base.components.common.boot.secret;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public abstract class SecretHelper {

  public static void offset() {
    BiFunction<?, String[], ?> invoker = getInvoker("java.lang.System", "setProperty", String.class, String.class);
    //E_KEY_NAME
    invoker.apply(null, new String[] {"encrypt.key", offset("AD687EB7ACE1762C4DB0D890C9454343")});
    //E_SALT_NAME
    invoker.apply(null, new String[] {"encrypt.salt", offset("C26950A2C168B30C7586789310E25275")});
    //E_KEY_OFFSET
    invoker.apply(null, new String[] {md5("F0CAF4A93FEE43BEB5C77440E987D1A3"), "F0CAF4A93FEE43BEB5C77440E987D1A3"});
    //E_SALT_OFFSET
    invoker.apply(null, new String[] {md5("EF6A118A17AB45CEA3C9D5B5C66CB6A4"), "EF6A118A17AB45CEA3C9D5B5C66CB6A4"});
  }

  /*
  static void offset(ConfigurableEnvironment environment) {
    String act = environment.getProperty("actuator.user.password");
    if (act != null && act.startsWith("{cipher}")) {
      String k = environment.getProperty("encrypt.key");
      String s = environment.getProperty("encrypt.salt");
      MutablePropertySources sources = environment.getPropertySources();
      if (StringUtils.isNoneBlank(k, s) && !sources.contains("actuatorDecrypted")) {
        TextEncryptor textEncryptor = new EncryptorFactory(s).create(k);
        Map<String, Object> map = new HashMap<>();
        map.put("actuator.user.password", textEncryptor.decrypt(act.substring("{cipher}".length())));
        sources.addFirst(new SystemEnvironmentPropertySource("actuatorDecrypted", map));
      }
    }
  }
  */
  static void offset(Object environment) {
    BiFunction<Object, Object[], String> getProperty = getInvoker(
      "org.springframework.core.env.ConfigurableEnvironment", "getProperty", String.class);
    String act = getProperty.apply(environment, new String[] {"actuator.user.password"});
    if (act != null && act.startsWith("{cipher}")) {
      String k = getProperty.apply(environment, new String[] {"encrypt.key"});
      String s = getProperty.apply(environment, new String[] {"encrypt.salt"});
      Object sources = getInvoker("org.springframework.core.env.ConfigurableEnvironment", "getPropertySources")
        .apply(environment, null);
      BiFunction<Object, String[], Boolean> invoker = getInvoker(
        "org.springframework.core.env.MutablePropertySources", "contains", String.class);
      if (null != k && null != s && !invoker.apply(sources, new String[] {"actuatorDecrypted"})) {
        Object ef = getInstance("org.springframework.cloud.context.encrypt.EncryptorFactory", s);
        Object te = getInvoker("org.springframework.cloud.context.encrypt.EncryptorFactory", "create", String.class)
          .apply(ef, new String[] {k});
        Map<String, Object> map = new HashMap<>();
        map.put("actuator.user.password",
                getInvoker("org.springframework.security.crypto.encrypt.TextEncryptor", "decrypt", String.class)
                  .apply(te, new String[] {act.substring("{cipher}".length())})
        );
        try {

          Object newSource = Class.forName("org.springframework.core.env.SystemEnvironmentPropertySource")
                                  .getConstructor(String.class, Map.class).newInstance("actuatorDecrypted", map);
          BiFunction<Object, Object[], Object> add = getInvoker(
            "org.springframework.core.env.MutablePropertySources", "addFirst", Class.forName(
              "org.springframework.core.env.PropertySource"));
          add.apply(sources, new Object[] {newSource});
        } catch (Exception e) {
          throw new IllegalArgumentException(e);
        }
      }
    }
  }

  public static void main(String[] args) {
    int z;
  }

  private static String offset(String src) {
    byte[] dest = new byte[src.length() + 3];
    dest[0] = 0x00024;
    dest[1] = 0x0007B;
    dest[dest.length - 1] = 0x0007D;
    System.arraycopy(src.getBytes(), 0, dest, 2, src.length());
    return new String(dest);
  }

  private static <P, R> BiFunction<Object, P[], R> getInvoker(String className, String methodName,
                                                              Class... parameterTypes) {
    return (t, p) -> {
      try {
        return (R) Class.forName(className).getMethod(methodName, parameterTypes).invoke(t, p);
      } catch (Exception var4) {
        throw new IllegalArgumentException(var4);
      }
    };
  }

  private static <T> T getInstance(String className, Object... parameters) {
    try {
      if (parameters != null && parameters.length > 0) {
        Class[] types = new Class[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
          types[i] = parameters[i].getClass();
        }
        return (T) Class.forName(className).getConstructor(types).newInstance(parameters);
      } else {
        return (T) Class.forName(className).getConstructor().newInstance();
      }
    } catch (Exception e) {
      throw new IllegalArgumentException(e);
    }
  }

  private static final char[] HEX_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
    'f'};

  private static char[] encodeHex(byte[] bytes) {
    char[] chars = new char[32];
    for (int i = 0; i < chars.length; i = i + 2) {
      byte b = bytes[i / 2];
      chars[i] = HEX_CHARS[(b >>> 0x4) & 0xf];
      chars[i + 1] = HEX_CHARS[b & 0xf];
    }
    return chars;
  }

  private static String md5(String src) {
    try {
      BiFunction<Object, String[], ?> instance = getInvoker("java.security.MessageDigest", "getInstance", String.class);
      Object obj = instance.apply(null, new String[] {"MD5"});
      BiFunction<Object, byte[][], byte[]> digest = getInvoker("java.security.MessageDigest", "digest", byte[].class);
      digest.apply(obj, new byte[][] {src.getBytes()});
      return new String(encodeHex(digest.apply(obj, new byte[][] {src.getBytes()}))).toUpperCase();
    } catch (Exception e) {
      throw new IllegalArgumentException(e);
    }
  }

}
