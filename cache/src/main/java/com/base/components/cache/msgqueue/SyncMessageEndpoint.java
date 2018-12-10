
package com.base.components.cache.msgqueue;

import org.apache.commons.lang3.StringUtils;

/**
 * SyncMessageEndpoint
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-05-17 10:03
 */
public enum SyncMessageEndpoint {
  SENDER, LISTENER;

  public static final String CACHE_SPLIT = "-";
  public static final String SYNC_PREFIX = "97fd000e99f446b692282c8b738f3cab";
  public static final String SYNC_RETURN_VALUE_PREFIX = "770752cb94ce4722a3f070c2252d2c81";

  /**
   * Status
   * @author <a href="drakelee1221@gmail.com">LiGeng</a>
   * @version 1.0.0, 2018-05-17 10:03
   */
  public enum Status{
    WAITING, COMMIT, ROLLBACK, EXCEPTION
  }

  public static boolean isSyncCacheKey(String eventId){
    return eventId.startsWith(SYNC_PREFIX);
  }

  public static String buildCacheKey(String eventId){
    return SYNC_PREFIX + eventId;
  }

  public static String cleanSyncPrefix(String eventId){
    return eventId.replace(SYNC_PREFIX, "");
  }

  public static String buildCacheValue(SyncMessageEndpoint endpoint, Status status){
    return endpoint + CACHE_SPLIT + status;
  }

  public static String buildReturnValueCacheKey(String eventId){
    return SYNC_RETURN_VALUE_PREFIX + eventId;
  }

  public static SyncMessageEndpoint getEndpoint(Object cacheString){
    if(cacheString != null){
      String s = cacheString.toString();
      if(StringUtils.isNotBlank(s)){
        for (SyncMessageEndpoint endpoint : SyncMessageEndpoint.values()) {
          if(s.startsWith(endpoint.toString())){
            return endpoint;
          }
        }
      }
    }
    return null;
  }

  public static Status getStatus(Object cacheString){
    if(cacheString != null){
      String s = cacheString.toString();
      if(StringUtils.isNotBlank(s)){
        for (Status status : Status.values()) {
          if(s.endsWith(status.toString())){
            return status;
          }
        }
      }
    }
    return null;
  }
}
