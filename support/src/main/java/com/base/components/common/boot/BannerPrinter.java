package com.base.components.common.boot;

import org.springframework.boot.ResourceBanner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ClassUtils;

import java.io.PrintStream;


/**
 * BannerPrinter
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-01-25 11:55
 */
public class BannerPrinter extends ResourceBanner {
  private static final String DEFAULT_BANNER_LOCATION = "custom-banner.txt";

  private BannerPrinter(Resource resource) {
    super(resource);
  }

  public static BannerPrinter create(){
    ResourceLoader resourceLoader = new DefaultResourceLoader(ClassUtils.getDefaultClassLoader());
    Resource resource = resourceLoader.getResource(DEFAULT_BANNER_LOCATION);
    if (resource.exists()) {
      return new BannerPrinter(resource);
    }
    return null;
  }

  @Override
  public void printBanner(Environment environment, Class<?> sourceClass, PrintStream out) {
    SpringContextUtil.addLastStartedEvents(new EventHandler<ApplicationContext, Object>() {
      @Override
      public String getId() {
        return "BannerPrinter print";
      }

      @Override
      public Object onEvent(ApplicationContext applicationContext) {
        printBannerSuper(environment, sourceClass, out);
        return null;
      }
    });
  }

  private void printBannerSuper(Environment environment, Class<?> sourceClass, PrintStream out) {
    super.printBanner(environment, sourceClass, out);
  }
}
