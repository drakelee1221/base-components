package com.base.components.common.exception;

import brave.propagation.CurrentTraceContext;
import com.base.components.common.util.UUIDUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * ExceptionIdCreator
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-03-06 10:02
 */
@Component
@Order
public class ExceptionIdCreator {

  private CurrentTraceContext currentTraceContext;

  public ExceptionIdCreator(ApplicationContext applicationContext) {
    try {
      Class<?> tracerClass = Class.forName("brave.Tracer");
      if(tracerClass != null) {
        Object tracer = applicationContext.getBean(tracerClass);
        if(tracer != null){
          Field currentTraceContextField = tracerClass.getDeclaredField("currentTraceContext");
          currentTraceContextField.setAccessible(true);
          currentTraceContext = (CurrentTraceContext) currentTraceContextField.get(tracer);
        }
      }
    } catch (Exception ignore) {
    }
  }

  public String getCurrentTraceId(){
    String id = null;
    if(currentTraceContext != null ){
      try {
        Object traceContext = currentTraceContext.get();
        if(traceContext != null){
          id = traceContext.toString().replace("/", " ");
        }
      } catch (Exception ignore) {
      }
    }else{
      id = UUIDUtils.generateKey();
    }
    return id;
  }
}
