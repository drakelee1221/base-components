package com.base.components.common.rpc.ribbon;

import com.base.components.common.constants.rpc.RegistryMetadataKeys;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.ZoneAvoidanceRule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * CustomRibbonConfiguration
 * <pre>
 *    feign模块默认已引入此配置（CustomFeignConfiguration）；
 *    如未依赖feign模块，则需要在启动类上使用：@RibbonClients(defaultConfiguration = CustomRibbonConfiguration.class)
 * </pre>
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-07-05 15:32
 */
public class CustomRibbonConfiguration {

  @Bean
  @ConditionalOnProperty(value = "eureka.instance.metadata-map." + RegistryMetadataKeys.GROUP_TAGS_ENABLE_KEY, havingValue = "true")
  public IRule ribbonRule(IClientConfig clientConfig) {
    try {
      Class.forName("com.netflix.niws.loadbalancer.DiscoveryEnabledServer");
      return new GroupMarkFilterEurekaRibbonRule(clientConfig);
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException("配置失败 > [RPC负载均衡规则：根据Tag标签进行分组调用]", e);
    }
  }

  private IRule defaultRule(IClientConfig clientConfig){
    ZoneAvoidanceRule rule = new ZoneAvoidanceRule();
    rule.initWithNiwsConfig(clientConfig);
    return rule;
  }

}
