package com.base.components.common.exception;

import java.util.Map;

/**
 * ExternalFeignClientServices
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-02-27 15:06
 */
public interface ExternalFeignClientServices {

  String getHost(String externalServiceName);

  Map<String, String> getAllExternalServices();

}
