package com.base.components.common.constants.rpc;

/**
 * RegistryMetadataKeys
 * 如： eureka.instance.metadata-map[key]
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-07-06 13:51
 */
public interface RegistryMetadataKeys {
  /**
   * [RPC负载调用规则：Tag分组调用]，是否开启
   */
  String GROUP_TAGS_ENABLE_KEY = "group-tag-filter-rule-enable";

  /**
   * [RPC负载调用规则：Tag分组调用]
   * 当前节点分组过滤标签，多个以英文逗号隔开，默认为空
   */
  String GROUP_TAGS_KEY = "group-tag-filter-rule-tags";

  /**
   * [RPC负载调用规则：Tag分组调用]
   * 当前节点分组过滤标签为空时，是否限制调用其它有标签的节点，
   * true=限制只能调用其它标签为空的节点，false=不做任何限制（默认）
   */
  String NO_GROUP_TAGS_LIMIT_KEY = "group-tag-filter-rule-no-tags-limit";
}
