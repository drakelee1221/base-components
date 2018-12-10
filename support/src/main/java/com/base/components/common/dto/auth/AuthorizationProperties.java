package com.base.components.common.dto.auth;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

import static com.base.components.common.dto.auth.AuthorizationProperties.CheckType.*;

/**
 * AuthorizationProperties
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-06-19 14:08
 */
public class AuthorizationProperties {

  /**
   * 不验证token权限的url前缀（读取token），优先级：3
   */
  private List<String> notCheckUrls = Lists.newArrayList();

  /**
   * 静态资源路径前缀（跳过读取token），优先级：2
   */
  private List<String> staticResourceUriPrefix = Lists.newArrayList();

  /**
   * 静态资源路径后缀（跳过读取token），优先级：1
   */
  private List<String> staticResourceSuffix = Lists.newArrayList();


  public List<String> getNotCheckUrls() {
    return notCheckUrls;
  }

  public void setNotCheckUrls(List<String> notCheckUrls) {
    this.notCheckUrls = notCheckUrls;
  }

  public List<String> getStaticResourceUriPrefix() {
    return staticResourceUriPrefix;
  }

  public void setStaticResourceUriPrefix(List<String> staticResourceUriPrefix) {
    this.staticResourceUriPrefix = staticResourceUriPrefix;
  }

  public List<String> getStaticResourceSuffix() {
    return staticResourceSuffix;
  }

  public void setStaticResourceSuffix(List<String> staticResourceSuffix) {
    this.staticResourceSuffix = staticResourceSuffix;
  }

  public enum CheckType{
    /** 不从缓存中获取token对象 */
    NONE,

    /** 从缓存中获取token对象，不验证只往下游传递 */
    ONLY_TRANSFER,

    /** 从缓存中获取token对象，验证并往下游传递 */
    STRICT
  }

  /**
   * 获取检查token的类别
   * @param uri uri
   * @return CheckType
   */
  public CheckType checkTokenType(String uri){
    if (StringUtils.isNotBlank(uri)) {
      // fori 比 foreach 效率更高
      for (int i = 0; i < staticResourceSuffix.size(); i++) {
        if (uri.endsWith(staticResourceSuffix.get(i))) {
          return NONE;
        }
      }
      for (int i = 0; i < staticResourceUriPrefix.size(); i++) {
        if (uri.startsWith(staticResourceUriPrefix.get(i))) {
          return NONE;
        }
      }
      for (int i = 0; i < notCheckUrls.size(); i++) {
        if (uri.startsWith(notCheckUrls.get(i))) {
          return ONLY_TRANSFER;
        }
      }
    }
    return STRICT;
  }
}
