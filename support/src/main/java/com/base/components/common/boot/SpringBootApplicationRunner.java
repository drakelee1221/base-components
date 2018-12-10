package com.base.components.common.boot;

import com.base.components.common.boot.secret.SecretHelper;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.util.Assert;

import java.util.Set;

/**
 * SpringBootApplicationRunner
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-01-25 13:34
 */
public class SpringBootApplicationRunner {

  private static String PROJECT_PACKAGE_PREFIX;

  public static String getProjectPackagePrefix(){
    return PROJECT_PACKAGE_PREFIX;
  }

  public static ConfigurableApplicationContext run(Class<?> source, String... args) {
    return run(new Class<?>[] {source}, args);
  }

  public static ConfigurableApplicationContext run(Class<?>[] source, String[] args) {
    return run(source, null, args);
  }

  public static ConfigurableApplicationContext run(Class<?> source, Banner banner, String... args) {
    return run(new Class<?>[] {source}, banner, args);
  }

  public static ConfigurableApplicationContext run(Class<?>[] source, Banner banner, String[] args) {
    checkProjectPackagePrefix(source);
    SecretHelper.offset();
    RunnerCheckHelper.runnerCheck(args);
    //TODO 在远程调用其它项目时，如果返回了其它自定义header会出现411，防止411 见 sun.net.www.protocol.http.HttpURLConnection
    System.setProperty("sun.net.http.allowRestrictedHeaders", Boolean.TRUE.toString());
    SpringApplication app = new SpringApplication(source);
    app.addListeners(new SpringContextUtil.SetContextOnReadyEvent(),
                     new SpringContextUtil.RestartResetInitEvent());
    app.setBanner(banner);
    return app.run(args);
  }

  private static void checkProjectPackagePrefix(Class<?>[] mainClasses){
    for (Class<?> mainClass : mainClasses) {
      if(PROJECT_PACKAGE_PREFIX == null){
        ProjectPackagePrefix prefix = mainClass.getAnnotation(ProjectPackagePrefix.class);
        if(prefix != null && StringUtils.isNotBlank(prefix.value())){
          PROJECT_PACKAGE_PREFIX = clearPackage(prefix.value());
        }
        else{
          Set<String> sets = Sets.newHashSet();
          SpringBootApplication app = mainClass.getAnnotation(SpringBootApplication.class);
          if(app != null && app.scanBasePackages().length > 0){
            for (String s : app.scanBasePackages()) {
              sets.add(clearPackage(s));
            }
          }
          ComponentScan scan = mainClass.getAnnotation(ComponentScan.class);
          if(scan != null && scan.basePackages().length > 0){
            for (String s : scan.basePackages()) {
              sets.add(clearPackage(s));
            }
          }
          String name = mainClass.getName();
          for (String s : sets) {
            if(StringUtils.isNotBlank(s) && name.startsWith(s)){
              PROJECT_PACKAGE_PREFIX = s;
              break;
            }
          }
        }
      }
    }
    Assert.hasText(PROJECT_PACKAGE_PREFIX, "未获取到项目包名前缀！");
  }

  private static String clearPackage(String packageString){
    if (packageString.contains(".*")) {
      return packageString.substring(0, packageString.indexOf(".*"));
    }
    return packageString;
  }
}
