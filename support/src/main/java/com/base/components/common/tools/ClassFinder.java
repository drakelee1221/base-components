package com.base.components.common.tools;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Set;

/**
 * ClassFinder
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-09-07 17:20
 */
public abstract class ClassFinder {

  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public static Set<Class<?>> findWithPackage(String packageString) {
    Set<Class<?>> classes = Sets.newHashSet();
    PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    String p = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + packageString.replace(".", "/") + "/*";
    Resource[] resources;
    try {
      resources = resolver.getResources(p);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    for (Resource resource : resources) {
      String fileName = resource.getFilename();
      if (fileName != null && fileName.endsWith(".class")) {
        try {
          Class<?> c = Class.forName(packageString + "." + fileName.substring(0, fileName.indexOf(".")));
          classes.add(c);
        } catch (Exception e) {
          logger.warn("", e);
        }
      }
    }
    return classes;
  }
}
