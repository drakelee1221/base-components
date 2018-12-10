package com.base.components.common.token;

import com.base.components.common.boot.SpringBootApplicationRunner;
import com.base.components.common.tools.ClassFinder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * TokenTypes - 使用枚举类实现此接口,
 * 实现的枚举类应在包路径下： SpringBootApplicationRunner.getProjectPackagePrefix() + ".common.token"
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-09-12 17:04
 */
public interface TokenTypes {

  String name();

  /** 请求中token值所在header或参数中的key */
  String getTokenKey();

  /** 请求转发时将缓存中token对象序列化后, 传递到下游服务的参数key */
  String getReceiveJsonKey();

  /** 一种类型对应一个枚举 */
  Class<? extends TokenCacheObj> getTypeClass();

  /** token 来源 */
  TokenSrcType getTokenSrcType();

  static TokenTypes getType(Class<? extends TokenCacheObj> typeClass) {
    if (typeClass != null) {
      for (TokenTypes tokenType : getAllTokenTypes()) {
        if (typeClass.equals(tokenType.getTypeClass())) {
          return tokenType;
        }
      }
    }
    return null;
  }

  static Serializable getTokenInRequest(HttpServletRequest request, TokenTypes tokenType) {
    switch (tokenType.getTokenSrcType()) {
      case HEADER:
        return request.getHeader(tokenType.getTokenKey());
      case COOKIE: {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
          for (Cookie cookie : cookies) {
            if (cookie.getName().equals(tokenType.getTokenKey())) {
              return cookie.getValue();
            }
          }
        }
      }
      default:
    }
    return null;
  }

  static Serializable getTokenInRequest(ServerHttpRequest request, TokenTypes tokenType) {
    switch (tokenType.getTokenSrcType()) {
      case HEADER:
        return request.getHeaders().getFirst(tokenType.getTokenKey());
      case COOKIE: {
        for (List<HttpCookie> cookies : request.getCookies().values()) {
          for (HttpCookie cookie : cookies) {
            if (cookie.getName().equals(tokenType.getTokenKey())) {
              return cookie.getValue();
            }
          }
        }
      }
      default:
    }
    return null;
  }


  static Set<TokenTypes> getAllTokenTypes() {
    return TokenTypes.AllTokenTypes.ALL_TOKEN_TYPES;
  }

  class AllTokenTypes {
    private static Set<TokenTypes> ALL_TOKEN_TYPES;

    static {
      Class<TokenTypes> channelClass = TokenTypes.class;
      Class<Enum> enumClass = Enum.class;
      String pkg = SpringBootApplicationRunner.getProjectPackagePrefix() + ".common.token";
      Set<TokenTypes> channels = Sets.newLinkedHashSet();
      try {
        for (Class<?> c : ClassFinder.findWithPackage(pkg)) {
          if (c != channelClass && channelClass.isAssignableFrom(c) && enumClass.isAssignableFrom(c)) {
            Method method = c.getMethod("values");
            method.setAccessible(true);
            TokenTypes[] values = (TokenTypes[]) method.invoke(null);
            channels.addAll(Arrays.asList(values));
          }
        }
      } catch (Exception ignore) {
      }
      ALL_TOKEN_TYPES = ImmutableSet.copyOf(channels);
    }
  }



  enum TokenSrcType {
    HEADER,
    COOKIE
  }
}
