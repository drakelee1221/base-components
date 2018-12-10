package com.base.components.common.boot;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.env.PropertiesPropertySourceLoader;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

import java.io.FileNotFoundException;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * RunnerCheckHelper
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-06-12 16:30
 */
public abstract class RunnerCheckHelper {
  public static final String PROFILE_ACTIVE = "spring.profiles.active";
  private static final String[] PROFILE_SOURCE_NAME = {"bootstrap", "application"};
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final String[] OFF_LOG_CLASS = {"org.springframework.boot.env.OriginTrackedYamlLoader"};
  private static final String PLACEHOLDER_PREFIX = "${";
  private static final String PLACEHOLDER_SUFFIX = "}";
  private static final String EUREKA_ZONES_DEFAULT_HAS_BLANKS = "eureka-zones-default-has-blanks";
  private static final String EUREKA_ZONES_DEFAULT_TRIM_BLANKS = "eureka-zones-default-trim-blanks";
  private static final String APP_NAME = "spring.application.name";
  private static final String APP_VERSION = "info.app.version";
  private static final String PATH_PARENT_LEVEL_KEY = "base.fuzzy.command.path.parent.level";
  private static final int PATH_PARENT_LEVEL = 2;

  public static void runnerCheck(String[] args){
    Map<ch.qos.logback.classic.Logger, Level> offMap = offLogLevel();

    ApplicationArguments arguments = new DefaultApplicationArguments(args);
    List<PropertySource<?>> sources = Lists.newArrayList();
    sources.addAll(loadSource(new YamlPropertySourceLoader(), ".yml"));
    sources.addAll(loadSource(new PropertiesPropertySourceLoader(), ".properties"));
    checkProfiles(arguments, sources);
    fuzzyBootPath(sources);
    trimEurekaZones(getValue(sources, EUREKA_ZONES_DEFAULT_HAS_BLANKS));

    resumeOffLogLevel(offMap);
  }

  /**
   * spring.profiles.active 全部累加
   */
  private static void checkProfiles(ApplicationArguments arguments, List<PropertySource<?>> sources) {
    Set<String> profiles = new LinkedHashSet<>();
    appendProfiles(profiles, arguments.getOptionValues(PROFILE_ACTIVE));
    appendProfiles(profiles, System.getProperty(PROFILE_ACTIVE));
    appendProfiles(profiles, getValue(sources, PROFILE_ACTIVE));
    String p = StringUtils.join(profiles, ",");
    System.setProperty(PROFILE_ACTIVE, p);
    logger.debug(PROFILE_ACTIVE + " > " + p);
  }


  /**
   * 优先级 启动参数 > 系统参数 > bootstrap.xxx > application.xxx
   */
  private static String getValue(ApplicationArguments arguments, List<PropertySource<?>> sources, String name){
    String val = getValue(arguments, name);
    if(val == null){
      val = System.getProperty(name);
    }
    if(val == null){
      val = getValue(sources, name);
    }
    if(val != null && val.startsWith(PLACEHOLDER_PREFIX) && val.endsWith(PLACEHOLDER_SUFFIX)){
      String val0 = getValue(arguments, sources, replacePlaceholder(val));
      if(val0 == null){
        throw new IllegalArgumentException(val  + " can not find value to replace placeholder !");
      }
      return val0;
    }
    return val;
  }

  private static String replacePlaceholder(String src){
    src = src.substring(PLACEHOLDER_PREFIX.length(), src.length());
    src = src.substring(0,  src.length() - PLACEHOLDER_SUFFIX.length());
    return src;
  }

  private static String getValue(ApplicationArguments arguments, String name){
    List<String> values = arguments.getOptionValues(name);
    if (values != null) {
      for (String value : values) {
        if (StringUtils.isNotBlank(value)){
          return value;
        }
      }
    }
    return null;
  }

  private static void appendProfiles(Set<String> profiles, List<String> profilesStrArray) {
    if (profilesStrArray != null) {
      for (String s : profilesStrArray) {
        appendProfiles(profiles, s);
      }
    }
  }

  private static void appendProfiles(Set<String> profiles, String profilesStr) {
    if(profilesStr != null){
      String[] split = StringUtils.split(profilesStr, ",");
      if (split != null) {
        profiles.addAll(Arrays.asList(split));
      }
    }
  }

  private static List<PropertySource<?>> loadSource(PropertySourceLoader loader, String suffix) {
    List<PropertySource<?>> sources = Lists.newArrayList();
    for (String sourceName : PROFILE_SOURCE_NAME) {
      try {
        sources.addAll(
          loader.load(sourceName, new ClassPathResource(sourceName + suffix))
        );
      } catch (FileNotFoundException ignore) {
      } catch (Exception e) {
        logger.error("", e);
      }
    }
    return sources;
  }

  private static String getValue(List<PropertySource<?>> sources, String name) {
    for (PropertySource<?> source : sources) {
      Object val = source.getProperty(name);
      if (val != null && val instanceof String) {
        return val.toString();
      }
    }
    return null;
  }


  private static Map<ch.qos.logback.classic.Logger, Level> offLogLevel(){
    Map<ch.qos.logback.classic.Logger, Level> map = Maps.newHashMap();
    for (String offLogClass : OFF_LOG_CLASS) {
      LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
      ch.qos.logback.classic.Logger logger = loggerContext
        .getLogger(offLogClass);
      if(logger != null){
        map.put(logger, logger.getLevel());
        logger.setLevel(Level.OFF);
      }
    }
    return map;
  }

  private static void resumeOffLogLevel(Map<ch.qos.logback.classic.Logger, Level> map){
    for (Map.Entry<ch.qos.logback.classic.Logger, Level> entry : map.entrySet()) {
      entry.getKey().setLevel(entry.getValue());
    }
  }

  /** 删除注册中心集群地址间的空白符号 */
  private static void trimEurekaZones(String srcZones){
    if(StringUtils.isNotBlank(srcZones)){
      System.setProperty(EUREKA_ZONES_DEFAULT_TRIM_BLANKS, StringUtils.deleteWhitespace(srcZones));
    }
  }

  /** 让注册中心显示启动Jar文件的路径 */
  private static void fuzzyBootPath(List<PropertySource<?>> sources){
    try {
      URL resource = logger.getClass().getClassLoader().getResource("");
      if(resource != null){
        String path = resource.getPath();
        if(StringUtils.isNotBlank(path)){
          String app = getValue(sources, APP_NAME) + "-" + getValue(sources, APP_VERSION);
          int index = path.indexOf(app);
          if(index >= 0){
            path = path.substring(0, index);
          }
          int level = PATH_PARENT_LEVEL;
          try {
            level = Integer.parseInt(Objects.requireNonNull(getValue(sources, PATH_PARENT_LEVEL_KEY)));
          } catch (Exception ignore) {
          }
          if(path.endsWith("/")){
            level++;
          }
          StringBuilder newPath = new StringBuilder();
          for (int i = 0; i < level; i++) {
            int last = path.lastIndexOf("/");
            if(last < 0){
              last = 0;
            }
            newPath.insert(0, path.substring(last));
            path = path.substring(0, last);
          }
          System.setProperty("fuzzy.boot.path", "*" + newPath.toString());
        }
      }
    } catch (Exception ignore) {
    }
  }
}
