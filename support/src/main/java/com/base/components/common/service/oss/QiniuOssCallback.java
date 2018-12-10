package com.base.components.common.service.oss;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * 七牛OSS回调接口，自定义回调业务方法实现此接口
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2017-07-29 11:57
 *
 */
public interface QiniuOssCallback {

  /**
   * 处理callback回调的方法
   * 访问地址如：/xxx/oss/qiniu/callback
   * @param param -
   */
  void callback(ObjectNode param);

  /**
   * 处理persistentNotify回调的方法
   * 访问地址如：/xxx/oss/qiniu/callback/persistent
   * @param param -
   */
  void persistentNotify(ObjectNode param);

  /**
   * 获取service的Key，用于生成token和回调时使用
   */
  String getServiceKey();
}
