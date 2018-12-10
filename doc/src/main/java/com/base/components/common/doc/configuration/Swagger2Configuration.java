package com.base.components.common.doc.configuration;

import com.base.components.common.doc.swagger2.DocumentationConfig;
import com.base.components.common.doc.swagger2.DynamicApiListingBuilderPlugin;
import com.base.components.common.doc.swagger2.DynamicResponseMessageBuilderPlugin;
import com.base.components.common.doc.swagger2.FieldPropertyBuilder;
import com.base.components.common.doc.swagger2.OperationRequestParamReader;
import com.google.common.base.Predicates;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.util.Map;

import static springfox.documentation.swagger.common.SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER;

/**
 * Swagger2Configuration
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-08-10 14:14
 */
@Configuration
@EnableSwagger2
@Profile("!prod")
@ConditionalOnProperty(name = "api-doc.swagger.enabled", havingValue = "true")
public class Swagger2Configuration {
  @Value("${info.app.name}")
  private String appName;

  @Value("${info.app.version}")
  private String appVersion;

  @Configuration
  @ConditionalOnProperty(name = "api-doc.swagger.gateway", havingValue = "false", matchIfMissing = true)
  public class Swagger2DocConfiguration {

    @Bean
    public Docket createRestApi() {
      // 创建API基本信息
      return new Docket(DocumentationType.SWAGGER_2)
        .ignoredParameterTypes(Map.class, ServletRequest.class, ServletResponse.class).forCodeGeneration(true)
        .apiInfo(apiInfo()).select()
        //只生成被ApiOperation这个注解注解过的api接口
        .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class)).paths(PathSelectors.any()).build();
    }

    private ApiInfo apiInfo() {
      return new ApiInfoBuilder().title("RESTful API").description("Module > " + appName).version(appVersion).build();
    }

    @Bean
    @Order
    public DynamicResponseMessageBuilderPlugin dynamicResponseMessageBuilderPlugin() {
      return new DynamicResponseMessageBuilderPlugin();
    }

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE - 1)
    public DynamicApiListingBuilderPlugin dynamicApiListingBuilderPlugin() {
      return new DynamicApiListingBuilderPlugin();
    }

    @Bean
    @Order(SWAGGER_PLUGIN_ORDER)
    public OperationRequestParamReader operationRequestBodyModelReader() {
      return new OperationRequestParamReader();
    }

    @Bean
    @Order(SWAGGER_PLUGIN_ORDER + 1)
    public OperationRequestParamReader operationRequestParamReader() {
      return new OperationRequestParamReader();
    }


    @Bean
    @Order(SWAGGER_PLUGIN_ORDER + 1)
    public FieldPropertyBuilder fieldPropertyBuilder() {
      return new FieldPropertyBuilder();
    }
  }


  @Configuration
  @ConditionalOnProperty(name = "api-doc.swagger.gateway", havingValue = "true")
  @ConfigurationProperties(value = "api-doc.swagger")
  public class Swagger2GatewayConfiguration {

    private Map<String, String> modules;

    @Bean
    public Docket createRestApi() {
      return new Docket(DocumentationType.SWAGGER_2).forCodeGeneration(true).select().apis(Predicates.alwaysFalse())
                                                    .build().apiInfo(apiInfo());
    }

    @Bean
    @Primary
    public SwaggerResourcesProvider swaggerResourcesProvider() {
      return new DocumentationConfig(modules);
    }

    private ApiInfo apiInfo() {
      return new ApiInfoBuilder().title("RESTful API").description("Module > " + appName).version(appVersion).build();
    }

    public Map<String, String> getModules() {
      return modules;
    }

    public void setModules(Map<String, String> modules) {
      this.modules = modules;
    }
  }

}
