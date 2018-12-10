package com.base.components.common.doc.swagger2;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;
import springfox.documentation.swagger2.web.Swagger2Controller;

import java.util.List;
import java.util.Map;

/**
 * DocumentationConfig - 网关路由各服务 api-docs Json 资源配置
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-08-24 11:20
 */
public class DocumentationConfig implements SwaggerResourcesProvider, InitializingBean {
  @Value("${info.app.version}")
  private String appVersion;
  @Autowired
  private Environment environment;

  private Map<String, String> modules;

  private Map<String, SwaggerResource> resources = Maps.newLinkedHashMap();

  public DocumentationConfig(Map<String, String> modules) {
    this.modules = modules;
  }

  @Override
  public List<SwaggerResource> get() {
    return Lists.newArrayList(resources.values());
  }

  private void addSwaggerResource(String key, String name) {
    String module = environment.getProperty(key);
    if (StringUtils.isNotBlank(module)) {
      SwaggerResource swaggerResource = new SwaggerResource();
      swaggerResource.setName(name + " ( " + module + " )");
      swaggerResource.setLocation("/" + module + Swagger2Controller.DEFAULT_URL);
      swaggerResource.setSwaggerVersion(appVersion);
      resources.put(module, swaggerResource);
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    for (Map.Entry<String, String> entry : modules.entrySet()) {
      addSwaggerResource(entry.getKey(), entry.getValue());
    }
  }
}
