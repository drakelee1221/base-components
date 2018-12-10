package com.base.components.common.rpc.ribbon;

import com.base.components.common.constants.rpc.RegistryMetadataKeys;
import com.base.components.common.util.SetsHelper;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.BaseLoadBalancer;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ZoneAvoidanceRule;
import com.netflix.niws.loadbalancer.DiscoveryEnabledServer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * CustomZoneAvoidanceRule
 * RPC负载均衡规则：根据Tag标签进行分组调用
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-07-04 19:41
 */
public class GroupMarkFilterEurekaRibbonRule extends ZoneAvoidanceRule {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public GroupMarkFilterEurekaRibbonRule() {
  }

  public GroupMarkFilterEurekaRibbonRule(IClientConfig clientConfig) {
    initWithNiwsConfig(clientConfig);
  }

  /**
   * 当前节点分组过滤标签，多个以英文逗号隔开，默认为空
   */
  @Value("${eureka.instance.metadata-map." + RegistryMetadataKeys.GROUP_TAGS_KEY + ":}")
  private String currentRuleTags;

  /**
   * 当前节点分组过滤标签为空时，是否限制调用其它有标签的节点，
   * true=限制只能调用其它标签为空的节点，false=不做任何限制（默认）
   */
  @Value("${eureka.instance.metadata-map." + RegistryMetadataKeys.NO_GROUP_TAGS_LIMIT_KEY + ":false}")
  private boolean noTagsLimit;

  @Override
  public Server choose(Object key) {
    ILoadBalancer lb = getLoadBalancer();
    List<Server> filtered = Collections.emptyList();
    Server choose = null;
    if (lb != null) {
      logger.debug((lb instanceof BaseLoadBalancer ? "targetClient: " + ((BaseLoadBalancer) lb).getName() + ", " : "")
                    + "currentRuleTags: " + currentRuleTags + ", noTagsLimit: " + noTagsLimit);
      Set<String> tags = SetsHelper.toStringSets(currentRuleTags, ",");
      boolean noMark = tags.isEmpty();
      //当前没有tag，且不做限制
      if (noMark && !noTagsLimit) {
        filtered = lb.getReachableServers();
      } else {
        List<Server> servers = lb.getReachableServers();
        filtered = servers.stream().filter(server -> {
          Map<String, String> metadata = ((DiscoveryEnabledServer) server).getInstanceInfo().getMetadata();
          String tag = metadata.get(RegistryMetadataKeys.GROUP_TAGS_KEY);
          if (noMark) {
            //当前没有tag，只调用没有tag的节点
            return StringUtils.isBlank(tag);
          } else {
            //当前有tag，比较是否都含有同一个tag
            return SetsHelper.existsSameElement(tags, SetsHelper.toStringSets(tag, ","));
          }
        }).collect(Collectors.toList());
      }
    }
    if (!filtered.isEmpty()) {
      choose = getPredicate().chooseRoundRobinAfterFiltering(filtered, key).orNull();
    }
    return choose;
  }
}
