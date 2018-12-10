package com.base.components.common.boot.secret;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.SystemEnvironmentPropertySource;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * BootInitializer
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-10-29 14:32
 */
@SuppressWarnings("all")
public class BootInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final String SANITIZES_SOURCES_NAME = "sanitizeSources";
  private static final String[] KEYS_TO_SANITIZE_KEYS = {"management.endpoint.env.keys-to-sanitize",
    "management.endpoint.configprops.keys-to-sanitize"};
  private static final String[] SANITIZES = {"password", "secret", "key", "token", ".*credentials.*", "vcap_services",
    "sun.java.command", "encrypt.*", ".*private.*"};

  @Override
  public void initialize(ConfigurableApplicationContext applicationContext) {
    ConfigurableEnvironment environment = applicationContext.getEnvironment();
    addSanitizes(environment);
    try {
      SecretHelper.offset(environment);
    } catch (Exception e) {
      logger.error("BootInitializer offset error !", e);
    }
  }

  private void addSanitizes(ConfigurableEnvironment environment) {
    MutablePropertySources sources = environment.getPropertySources();
    if(!sources.contains(SANITIZES_SOURCES_NAME)){
      Map<String, Object> addMap = new HashMap<>();
      Set<String> sanitizes = new HashSet<>();
      Collections.addAll(sanitizes, SANITIZES);
      for (PropertySource<?> propertySource : sources) {
        String name = propertySource.getName();
        for (String keysToSanitizeKey : KEYS_TO_SANITIZE_KEYS) {
          if (name.startsWith(keysToSanitizeKey)) {
            sanitizes.add(name);
            break;
          }
        }
      }
      int i = 0;
      for (String sanitize : sanitizes) {
        for (String keysToSanitizeKey : KEYS_TO_SANITIZE_KEYS) {
          addMap.put(keysToSanitizeKey + "\u005B" + i + "\u005D", sanitize);
        }
        i++;
      }
      sources.addFirst(new SystemEnvironmentPropertySource(SANITIZES_SOURCES_NAME, addMap));
    }
  }
}
