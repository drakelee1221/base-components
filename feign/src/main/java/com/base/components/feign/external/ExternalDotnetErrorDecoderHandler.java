/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.feign.external;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.base.components.common.constants.rpc.RpcHeaders;
import com.base.components.common.exception.ExceptionResponseEntity;
import com.base.components.common.util.JsonUtils;
import com.base.components.feign.configuration.AbstractErrorDecoderHandler;
import feign.Response;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Collection;

/**
 * ExternalDotnetErrorDecoderHandler
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-03-05 16:49
 *
 */
public class ExternalDotnetErrorDecoderHandler extends AbstractErrorDecoderHandler {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static class SingletonHolder{
    public static ExternalDotnetErrorDecoderHandler instance = new ExternalDotnetErrorDecoderHandler();
  }
  private ExternalDotnetErrorDecoderHandler(){}
  public static ExternalDotnetErrorDecoderHandler getInstance(){
    return SingletonHolder.instance;
  }

  @Override
  protected ExceptionResponseEntity customBuild(String methodKey, Response response, String body) {
    if(StringUtils.isNotBlank(body)){
      try {
        ObjectNode node = JsonUtils.reader(body, ObjectNode.class);
        ExceptionResponseEntity entity = new ExceptionResponseEntity();
        entity.setMessage(node.path("resultMsg").asText());
        entity.setException(node.path("errorNumber").asText());
        Collection<String> modules = response.request().headers().get(RpcHeaders.CLIENT_MARK_KEY);
        if(modules != null && !modules.isEmpty()){
          entity.setModule(StringUtils.join(modules, ""));
        }
        return entity;
      } catch (IOException e) {
        logger.error("", e);
      }
    }
    return null;
  }

}
