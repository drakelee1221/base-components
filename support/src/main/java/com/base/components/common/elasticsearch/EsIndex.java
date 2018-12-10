package com.base.components.common.elasticsearch;

/**
 * elastic search index
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2017-11-27 16:34
 */
public interface EsIndex {
  /**
   * 获取当前服务的 profiles.active
   * @return dev、prod、test
   */
  String getIndexProfile();

  /**
   * 获取当前项目名称 info.app.project
   * @return 如 he800
   */
  String getProjectName();

  String INDEX_PROFILE_PLACEHOLDER = "#{esIndex.getIndexProfile()}";

  String PROJECT_NAME_PLACEHOLDER = "#{esIndex.getProjectName()}";

  /** 索引前缀 */
  String INDEX_PREFIX = PROJECT_NAME_PLACEHOLDER + "_index_" + INDEX_PROFILE_PLACEHOLDER + "-";

}
