package com.base.components.common.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The type Http client util.
 */
public class HttpClientUtil {


  /**
   * post请求
   *
   * @param url the url
   * @param params the params
   *
   * @return string
   *
   * @throws Exception the exception
   */
  public static String post(String url, Map<String, String> params) throws Exception {
    String body;
    try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
      HttpPost post = postForm(url, params);
      body = invoke(httpclient, post);
    }
    return body;
  }

  /**
   * post请求
   * @param url
   * @param headers
   * @return
   * @throws Exception
   */
  public static String postHeadres(String url, Map<String, String> headers, Map<String, String> params) throws Exception{
    String body;
    try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
      HttpPost post = postForm(url, params);
      for (Map.Entry<String, String> entry : headers.entrySet()) {
        post.addHeader(entry.getKey(), entry.getValue());
      }
      body = invoke(httpclient, post);
    }
    return body;
  }

  /**
   * get请求
   *
   * @param url the url
   *
   * @return string
   *
   * @throws Exception the exception
   */
  public static String get(String url) throws Exception {
    String body = null;
    try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
      HttpGet get = new HttpGet(url);
      body = invoke(httpclient, get);
    }
    return body;
  }

  public static String get(String url, Map<String, String> headers) throws Exception {
    String body = null;
    try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
      HttpGet get = new HttpGet(url);
      for (Map.Entry<String, String> entry : headers.entrySet()) {
        get.addHeader(entry.getKey(), entry.getValue());
      }
      body = invoke(httpclient, get);
    }
    return body;
  }


  /**
   * Gets we char.
   *
   * @param url the url
   *
   * @return the we char
   *
   * @throws Exception the exception
   */
  public static String getWeChar(String url) throws Exception {
    String body = null;
    try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
      HttpGet get = new HttpGet(url);
      body = invoke(httpclient, get);
    }
    return body;
  }

  private static String invoke(CloseableHttpClient httpclient, HttpUriRequest httpost) throws Exception {
    HttpResponse response = sendRequest(httpclient, httpost);
    String body = paseResponse(response);
    return body;
  }

  /**
   * 请求返回结果
   *
   * @param response
   *
   * @return
   *
   * @throws Exception
   */
  private static String paseResponse(HttpResponse response) throws Exception {
    HttpEntity entity = response.getEntity();
    String body = null;
    body = EntityUtils.toString(entity);
    return body;
  }

  private static String paseResponse(HttpResponse response, String charset) throws Exception {
    HttpEntity entity = response.getEntity();
    String body = null;
    body = EntityUtils.toString(entity, charset);
    return body;
  }

  private static HttpResponse sendRequest(CloseableHttpClient httpclient, HttpUriRequest httpost) throws Exception {
    HttpResponse response = null;
    response = httpclient.execute(httpost);
    return response;
  }

  /**
   * post请求参数
   *
   * @param url
   * @param params
   *
   * @return
   *
   * @throws Exception
   */
  private static HttpPost postForm(String url, Map<String, String> params) throws Exception {
    HttpPost httpost = new HttpPost(url);
    List<NameValuePair> nvps = new ArrayList<NameValuePair>();
    Set<String> keySet = params.keySet();
    for (String key : keySet) {
      nvps.add(new BasicNameValuePair(key, params.get(key)));
    }
    httpost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
    return httpost;
  }

  /**
   * Send xml data by post string.
   *
   * @param url the url
   * @param xmlData the xml data
   *
   * @return the string
   */
  public static String sendXMLDataByPost(String url, String xmlData) {
    String body = "";
    try(CloseableHttpClient client = HttpClients.createDefault()) {
      HttpPost post = new HttpPost(url);
      // post.addHeader("Content-Type","text/html;charset=UTF-8");
      StringEntity entity = new StringEntity(xmlData, "UTF-8");
      post.setEntity(entity);
      HttpResponse response = client.execute(post);
        body = EntityUtils.toString(response.getEntity());
    } catch (IOException e) {
      e.printStackTrace();
    }
    return body;
  }

  /**
   * Post https string.
   *
   * @param url 请求地址
   * @param xmlData 请求xml数据
   * @param filePath 凭证路径
   * @param pwd 凭证密码
   *
   * @return string
   *
   * @throws Exception the exception
   */
  public static String postHttps(String url, String xmlData, String filePath, String pwd) throws Exception {
    KeyStore keyStore = KeyStore.getInstance("PKCS12");
    try (FileInputStream instream = new FileInputStream(new File(filePath))) {
      keyStore.load(instream, pwd.toCharArray());
    }
    
    SSLContext sslcontext = SSLContexts.custom().loadKeyMaterial(keyStore, pwd.toCharArray()).build();
    SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, new String[] {"TLSv1"}, null,
      SSLConnectionSocketFactory.getDefaultHostnameVerifier());
    HttpResponse httpResponse;
    try (CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build()) {
      HttpPost httpPost = new HttpPost(url);
      StringEntity entity = new StringEntity(xmlData, "UTF-8");
      httpPost.setEntity(entity);
      httpResponse = httpclient.execute(httpPost);
    }
    return EntityUtils.toString(httpResponse.getEntity());
  }

  /**
   * Post new https string.
   *
   * @param url the url
   * @param xmlData the xml data
   * @param filePath the file path
   * @param pwd the pwd
   *
   * @return the string
   *
   * @throws Exception the exception
   */
  public static String postNewHttps(String url, String xmlData, String filePath, String pwd) throws Exception {
    SSLContext sslcontext =
      SSLContexts.custom().loadTrustMaterial(new File(filePath), pwd.toCharArray(), new TrustSelfSignedStrategy())
        .build();
    SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, new String[] {"TLSv1"}, null,
      SSLConnectionSocketFactory.getDefaultHostnameVerifier());
    HttpResponse httpResponse;
    try (CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build()) {
      HttpPost httpPost = new HttpPost(url);
      StringEntity entity = new StringEntity(xmlData, "UTF-8");
      httpPost.setEntity(entity);
      httpResponse = httpclient.execute(httpPost);
    }
    return EntityUtils.toString(httpResponse.getEntity());
  }


}
