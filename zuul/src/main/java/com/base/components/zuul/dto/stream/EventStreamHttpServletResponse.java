package com.base.components.zuul.dto.stream;

import org.springframework.http.MediaType;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;

/**
 * EventStreamHttpServletResponse
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-07-12 14:37
 */
public class EventStreamHttpServletResponse extends HttpServletResponseWrapper {

  private EventStreamServletOutputStream outputStream;

  public static HttpServletResponse build(HttpServletResponse response, String mediaType){
    if (response != null && mediaType != null && mediaType.contains(MediaType.TEXT_EVENT_STREAM_VALUE)){
      return new EventStreamHttpServletResponse(response);
    }
    else{
      return response;
    }
  }

  private EventStreamHttpServletResponse(HttpServletResponse response) {
    super(response);
  }

  @Override
  public ServletOutputStream getOutputStream() throws IOException {
    if(outputStream == null){
      outputStream = new EventStreamServletOutputStream(super.getOutputStream());
    }
    return outputStream;
  }
}
