package com.base.components.common.configuration;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.SocketUtils;

/**
 * Custom System Properties Configuration
 *
 * @author <a href="huangchaofei@xianyunsoft.com">Huang Chaofei</a>
 * @version 1.0.0, 2017-06-30
 */
public enum CustomSystemPropertiesConfiguration {
  /**
   * Instance custom system properties configuration.
   */
  INSTANCE;

  private static final String APP_SHORT_NAME_KEY = "app.short.name";
  private static final String SERVER_PORT_KEY = "server.port";
  private static int DEFAULT_PORT_RANGE_MIN = 10000;



  /**
   * Config application short name custom system properties configuration.
   *
   * @param appShortName the app short name
   *
   * @return the custom system properties configuration
   */
  public CustomSystemPropertiesConfiguration configApplicationShortName(String appShortName) {
    if (StringUtils.isBlank(System.getProperty(APP_SHORT_NAME_KEY))) {
      System.setProperty(APP_SHORT_NAME_KEY, appShortName);
    }

    return this;
  }



  /**
   * Config application random server port custom system properties configuration.
   *
   * @param minPort the min port
   *
   * @return the custom system properties configuration
   */
  public CustomSystemPropertiesConfiguration configApplicationRandomServerPort(int minPort) {
    if (StringUtils.isBlank(System.getProperty(SERVER_PORT_KEY))) {
      System.setProperty(SERVER_PORT_KEY, String.valueOf(
        SocketUtils.findAvailableTcpPort(minPort < DEFAULT_PORT_RANGE_MIN ? DEFAULT_PORT_RANGE_MIN : minPort)));
    }

    return this;
  }


  /**
   * Config application random server port custom system properties configuration.
   *
   * @param minPort the min port
   * @param maxPort the max port
   *
   * @return the custom system properties configuration
   */
  public CustomSystemPropertiesConfiguration configApplicationRandomServerPort(int minPort, int maxPort) {
    if (StringUtils.isBlank(System.getProperty(SERVER_PORT_KEY))) {
      System.setProperty(SERVER_PORT_KEY, String.valueOf(SocketUtils
        .findAvailableTcpPort(minPort < DEFAULT_PORT_RANGE_MIN ? DEFAULT_PORT_RANGE_MIN : minPort,
          maxPort > SocketUtils.PORT_RANGE_MAX ? SocketUtils.PORT_RANGE_MAX : maxPort)));
    }

    return this;
  }


  /**
   * Config application server port custom system properties configuration.
   *
   * @param port the port
   *
   * @return the custom system properties configuration
   */
  public CustomSystemPropertiesConfiguration configApplicationServerPort(int port) {
    if (StringUtils.isBlank(System.getProperty(SERVER_PORT_KEY))) {
      System
        .setProperty(SERVER_PORT_KEY, String.valueOf(port < DEFAULT_PORT_RANGE_MIN ? DEFAULT_PORT_RANGE_MIN : port));
    }

    return this;
  }

  public CustomSystemPropertiesConfiguration configPort(String propertyKey, int minPort) {
    if (StringUtils.isBlank(System.getProperty(propertyKey))) {
      System
        .setProperty(propertyKey, String.valueOf(minPort < DEFAULT_PORT_RANGE_MIN ? DEFAULT_PORT_RANGE_MIN : minPort));
    }
    return this;
  }

  public CustomSystemPropertiesConfiguration configRandomPort(String propertyKey, int minPort) {
    if (StringUtils.isBlank(System.getProperty(propertyKey))) {
      System.setProperty(
        propertyKey, String.valueOf(
          SocketUtils.findAvailableTcpPort(minPort < DEFAULT_PORT_RANGE_MIN ? DEFAULT_PORT_RANGE_MIN : minPort)));
    }
    return this;
  }
}
