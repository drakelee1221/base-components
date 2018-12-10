package com.base.components.common.boot;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.Set;

/**
 * Profiles
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-07-26 14:33
 */
public enum Profiles {
  DEV, PROD, TEST
  ;

  public static boolean contains(Profiles...profile){
    Set<String> profiles = getProfilesSetToUpperCase();
    if(profile != null && profile.length > 0){
      for (Profiles p : profile) {
        if(profiles.contains(p.toString())){
          return true;
        }
      }
    }
    return false;
  }

  public static boolean contains(String...profile){
    Set<String> profiles = getProfilesSet();
    if(profile != null && profile.length > 0){
      for (String p : profile) {
        if(profiles.contains(p)){
          return true;
        }
      }
    }
    return false;
  }

  public static Profiles getProfiles(){
    Set<String> profiles = getProfilesSetToUpperCase();
    for (Profiles profile : values()) {
      if(profiles.contains(profile.toString())){
        return profile;
      }
    }
    return null;
  }

  public static Set<String> getProfilesSet(){
    String profilesStr = getProfilesStr();
    if(StringUtils.isBlank(profilesStr)){
      return Collections.emptySet();
    }
    return Sets.newHashSet(StringUtils.split(profilesStr, ","));
  }

  public static Set<String> getProfilesSetToLowerCase(){
    String profilesStr = getProfilesStr();
    if(StringUtils.isBlank(profilesStr)){
      return Collections.emptySet();
    }
    return Sets.newHashSet(StringUtils.split(profilesStr.toLowerCase(), ","));
  }

  public static Set<String> getProfilesSetToUpperCase(){
    String profilesStr = getProfilesStr();
    if(StringUtils.isBlank(profilesStr)){
      return Collections.emptySet();
    }
    return Sets.newHashSet(StringUtils.split(profilesStr.toUpperCase(), ","));
  }

  public static String getProfilesStr(){
    String p;
    try {
      p = SpringContextUtil.getContext().getEnvironment().getProperty(RunnerCheckHelper.PROFILE_ACTIVE);
    } catch (Exception e) {
      p = System.getProperty(RunnerCheckHelper.PROFILE_ACTIVE);
    }
    return p == null? "" : p;
  }

}
