package com.base.components.common.configuration.eureka;

import com.netflix.discovery.shared.transport.EurekaTransportConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.netflix.eureka.EurekaClientConfigBean;
import org.springframework.core.env.PropertyResolver;

import java.util.List;
import java.util.Map;

/**
 * TrimUrlBlanksEurekaClientConfigBean - 去掉注册中心 server url 前后的空白字符
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-09-14 10:42
 */
public class TrimUrlBlanksEurekaClientConfigBean extends EurekaClientConfigBean {
  private EurekaClientConfigBean delegate;

  public TrimUrlBlanksEurekaClientConfigBean(EurekaClientConfigBean delegate) {
    this.delegate = delegate;
    cleanUrl(this.delegate.getServiceUrl());
  }

  @Override
  public boolean shouldGZipContent() {
    return delegate.shouldGZipContent();
  }

  @Override
  public boolean shouldUseDnsForFetchingServiceUrls() {
    return delegate.shouldUseDnsForFetchingServiceUrls();
  }

  @Override
  public boolean shouldRegisterWithEureka() {
    return delegate.shouldRegisterWithEureka();
  }

  @Override
  public boolean shouldPreferSameZoneEureka() {
    return delegate.shouldPreferSameZoneEureka();
  }

  @Override
  public boolean shouldLogDeltaDiff() {
    return delegate.shouldLogDeltaDiff();
  }

  @Override
  public boolean shouldDisableDelta() {
    return delegate.shouldDisableDelta();
  }

  @Override
  public boolean shouldUnregisterOnShutdown() {
    return delegate.shouldUnregisterOnShutdown();
  }

  @Override
  public boolean shouldEnforceRegistrationAtInit() {
    return delegate.shouldEnforceRegistrationAtInit();
  }

  @Override
  public String fetchRegistryForRemoteRegions() {
    return delegate.fetchRegistryForRemoteRegions();
  }

  @Override
  public String[] getAvailabilityZones(String region) {
    return delegate.getAvailabilityZones(region);
  }

  @Override
  public List<String> getEurekaServerServiceUrls(String myZone) {
    return delegate.getEurekaServerServiceUrls(myZone);
  }

  @Override
  public boolean shouldFilterOnlyUpInstances() {
    return delegate.shouldFilterOnlyUpInstances();
  }

  @Override
  public boolean shouldFetchRegistry() {
    return delegate.shouldFetchRegistry();
  }

  @Override
  public boolean allowRedirects() {
    return delegate.allowRedirects();
  }

  @Override
  public boolean shouldOnDemandUpdateStatusChange() {
    return delegate.shouldOnDemandUpdateStatusChange();
  }

  @Override
  public String getExperimental(String name) {
    return delegate.getExperimental(name);
  }

  @Override
  public EurekaTransportConfig getTransportConfig() {
    return delegate.getTransportConfig();
  }

  @Override
  public PropertyResolver getPropertyResolver() {
    return delegate.getPropertyResolver();
  }

  @Override
  public void setPropertyResolver(PropertyResolver propertyResolver) {
    delegate.setPropertyResolver(propertyResolver);
  }

  @Override
  public boolean isEnabled() {
    return delegate.isEnabled();
  }

  @Override
  public void setEnabled(boolean enabled) {
    delegate.setEnabled(enabled);
  }

  @Override
  public EurekaTransportConfig getTransport() {
    return delegate.getTransport();
  }

  @Override
  public void setTransport(EurekaTransportConfig transport) {
    delegate.setTransport(transport);
  }

  @Override
  public int getRegistryFetchIntervalSeconds() {
    return delegate.getRegistryFetchIntervalSeconds();
  }

  @Override
  public void setRegistryFetchIntervalSeconds(int registryFetchIntervalSeconds) {
    delegate.setRegistryFetchIntervalSeconds(registryFetchIntervalSeconds);
  }

  @Override
  public int getInstanceInfoReplicationIntervalSeconds() {
    return delegate.getInstanceInfoReplicationIntervalSeconds();
  }

  @Override
  public void setInstanceInfoReplicationIntervalSeconds(int instanceInfoReplicationIntervalSeconds) {
    delegate.setInstanceInfoReplicationIntervalSeconds(instanceInfoReplicationIntervalSeconds);
  }

  @Override
  public int getInitialInstanceInfoReplicationIntervalSeconds() {
    return delegate.getInitialInstanceInfoReplicationIntervalSeconds();
  }

  @Override
  public void setInitialInstanceInfoReplicationIntervalSeconds(int initialInstanceInfoReplicationIntervalSeconds) {
    delegate.setInitialInstanceInfoReplicationIntervalSeconds(initialInstanceInfoReplicationIntervalSeconds);
  }

  @Override
  public int getEurekaServiceUrlPollIntervalSeconds() {
    return delegate.getEurekaServiceUrlPollIntervalSeconds();
  }

  @Override
  public void setEurekaServiceUrlPollIntervalSeconds(int eurekaServiceUrlPollIntervalSeconds) {
    delegate.setEurekaServiceUrlPollIntervalSeconds(eurekaServiceUrlPollIntervalSeconds);
  }

  @Override
  public String getProxyPort() {
    return delegate.getProxyPort();
  }

  @Override
  public void setProxyPort(String proxyPort) {
    delegate.setProxyPort(proxyPort);
  }

  @Override
  public String getProxyHost() {
    return delegate.getProxyHost();
  }

  @Override
  public void setProxyHost(String proxyHost) {
    delegate.setProxyHost(proxyHost);
  }

  @Override
  public String getProxyUserName() {
    return delegate.getProxyUserName();
  }

  @Override
  public void setProxyUserName(String proxyUserName) {
    delegate.setProxyUserName(proxyUserName);
  }

  @Override
  public String getProxyPassword() {
    return delegate.getProxyPassword();
  }

  @Override
  public void setProxyPassword(String proxyPassword) {
    delegate.setProxyPassword(proxyPassword);
  }

  @Override
  public int getEurekaServerReadTimeoutSeconds() {
    return delegate.getEurekaServerReadTimeoutSeconds();
  }

  @Override
  public void setEurekaServerReadTimeoutSeconds(int eurekaServerReadTimeoutSeconds) {
    delegate.setEurekaServerReadTimeoutSeconds(eurekaServerReadTimeoutSeconds);
  }

  @Override
  public int getEurekaServerConnectTimeoutSeconds() {
    return delegate.getEurekaServerConnectTimeoutSeconds();
  }

  @Override
  public void setEurekaServerConnectTimeoutSeconds(int eurekaServerConnectTimeoutSeconds) {
    delegate.setEurekaServerConnectTimeoutSeconds(eurekaServerConnectTimeoutSeconds);
  }

  @Override
  public String getBackupRegistryImpl() {
    return delegate.getBackupRegistryImpl();
  }

  @Override
  public void setBackupRegistryImpl(String backupRegistryImpl) {
    delegate.setBackupRegistryImpl(backupRegistryImpl);
  }

  @Override
  public int getEurekaServerTotalConnections() {
    return delegate.getEurekaServerTotalConnections();
  }

  @Override
  public void setEurekaServerTotalConnections(int eurekaServerTotalConnections) {
    delegate.setEurekaServerTotalConnections(eurekaServerTotalConnections);
  }

  @Override
  public int getEurekaServerTotalConnectionsPerHost() {
    return delegate.getEurekaServerTotalConnectionsPerHost();
  }

  @Override
  public void setEurekaServerTotalConnectionsPerHost(int eurekaServerTotalConnectionsPerHost) {
    delegate.setEurekaServerTotalConnectionsPerHost(eurekaServerTotalConnectionsPerHost);
  }

  @Override
  public String getEurekaServerURLContext() {
    return delegate.getEurekaServerURLContext();
  }

  @Override
  public void setEurekaServerURLContext(String eurekaServerURLContext) {
    delegate.setEurekaServerURLContext(eurekaServerURLContext);
  }

  @Override
  public String getEurekaServerPort() {
    return delegate.getEurekaServerPort();
  }

  @Override
  public void setEurekaServerPort(String eurekaServerPort) {
    delegate.setEurekaServerPort(eurekaServerPort);
  }

  @Override
  public String getEurekaServerDNSName() {
    return delegate.getEurekaServerDNSName();
  }

  @Override
  public void setEurekaServerDNSName(String eurekaServerDNSName) {
    delegate.setEurekaServerDNSName(eurekaServerDNSName);
  }

  @Override
  public String getRegion() {
    return delegate.getRegion();
  }

  @Override
  public void setRegion(String region) {
    delegate.setRegion(region);
  }

  @Override
  public int getEurekaConnectionIdleTimeoutSeconds() {
    return delegate.getEurekaConnectionIdleTimeoutSeconds();
  }

  @Override
  public void setEurekaConnectionIdleTimeoutSeconds(int eurekaConnectionIdleTimeoutSeconds) {
    delegate.setEurekaConnectionIdleTimeoutSeconds(eurekaConnectionIdleTimeoutSeconds);
  }

  @Override
  public String getRegistryRefreshSingleVipAddress() {
    return delegate.getRegistryRefreshSingleVipAddress();
  }

  @Override
  public void setRegistryRefreshSingleVipAddress(String registryRefreshSingleVipAddress) {
    delegate.setRegistryRefreshSingleVipAddress(registryRefreshSingleVipAddress);
  }

  @Override
  public int getHeartbeatExecutorThreadPoolSize() {
    return delegate.getHeartbeatExecutorThreadPoolSize();
  }

  @Override
  public void setHeartbeatExecutorThreadPoolSize(int heartbeatExecutorThreadPoolSize) {
    delegate.setHeartbeatExecutorThreadPoolSize(heartbeatExecutorThreadPoolSize);
  }

  @Override
  public int getHeartbeatExecutorExponentialBackOffBound() {
    return delegate.getHeartbeatExecutorExponentialBackOffBound();
  }

  @Override
  public void setHeartbeatExecutorExponentialBackOffBound(int heartbeatExecutorExponentialBackOffBound) {
    delegate.setHeartbeatExecutorExponentialBackOffBound(heartbeatExecutorExponentialBackOffBound);
  }

  @Override
  public int getCacheRefreshExecutorThreadPoolSize() {
    return delegate.getCacheRefreshExecutorThreadPoolSize();
  }

  @Override
  public void setCacheRefreshExecutorThreadPoolSize(int cacheRefreshExecutorThreadPoolSize) {
    delegate.setCacheRefreshExecutorThreadPoolSize(cacheRefreshExecutorThreadPoolSize);
  }

  @Override
  public int getCacheRefreshExecutorExponentialBackOffBound() {
    return delegate.getCacheRefreshExecutorExponentialBackOffBound();
  }

  @Override
  public void setCacheRefreshExecutorExponentialBackOffBound(int cacheRefreshExecutorExponentialBackOffBound) {
    delegate.setCacheRefreshExecutorExponentialBackOffBound(cacheRefreshExecutorExponentialBackOffBound);
  }

  @Override
  public Map<String, String> getServiceUrl() {
    return delegate.getServiceUrl();
  }

  @Override
  public void setServiceUrl(Map<String, String> serviceUrl) {
    cleanUrl(serviceUrl);
    super.setServiceUrl(serviceUrl);
  }

  @Override
  public boolean isgZipContent() {
    return delegate.isgZipContent();
  }

  @Override
  public void setgZipContent(boolean gZipContent) {
    delegate.setgZipContent(gZipContent);
  }

  @Override
  public boolean isUseDnsForFetchingServiceUrls() {
    return delegate.isUseDnsForFetchingServiceUrls();
  }

  @Override
  public void setUseDnsForFetchingServiceUrls(boolean useDnsForFetchingServiceUrls) {
    delegate.setUseDnsForFetchingServiceUrls(useDnsForFetchingServiceUrls);
  }

  @Override
  public boolean isRegisterWithEureka() {
    return delegate.isRegisterWithEureka();
  }

  @Override
  public void setRegisterWithEureka(boolean registerWithEureka) {
    delegate.setRegisterWithEureka(registerWithEureka);
  }

  @Override
  public boolean isPreferSameZoneEureka() {
    return delegate.isPreferSameZoneEureka();
  }

  @Override
  public void setPreferSameZoneEureka(boolean preferSameZoneEureka) {
    delegate.setPreferSameZoneEureka(preferSameZoneEureka);
  }

  @Override
  public boolean isLogDeltaDiff() {
    return delegate.isLogDeltaDiff();
  }

  @Override
  public void setLogDeltaDiff(boolean logDeltaDiff) {
    delegate.setLogDeltaDiff(logDeltaDiff);
  }

  @Override
  public boolean isDisableDelta() {
    return delegate.isDisableDelta();
  }

  @Override
  public void setDisableDelta(boolean disableDelta) {
    delegate.setDisableDelta(disableDelta);
  }

  @Override
  public String getFetchRemoteRegionsRegistry() {
    return delegate.getFetchRemoteRegionsRegistry();
  }

  @Override
  public void setFetchRemoteRegionsRegistry(String fetchRemoteRegionsRegistry) {
    delegate.setFetchRemoteRegionsRegistry(fetchRemoteRegionsRegistry);
  }

  @Override
  public Map<String, String> getAvailabilityZones() {
    return delegate.getAvailabilityZones();
  }

  @Override
  public void setAvailabilityZones(Map<String, String> availabilityZones) {
    delegate.setAvailabilityZones(availabilityZones);
  }

  @Override
  public boolean isFilterOnlyUpInstances() {
    return delegate.isFilterOnlyUpInstances();
  }

  @Override
  public void setFilterOnlyUpInstances(boolean filterOnlyUpInstances) {
    delegate.setFilterOnlyUpInstances(filterOnlyUpInstances);
  }

  @Override
  public boolean isFetchRegistry() {
    return delegate.isFetchRegistry();
  }

  @Override
  public void setFetchRegistry(boolean fetchRegistry) {
    delegate.setFetchRegistry(fetchRegistry);
  }

  @Override
  public String getDollarReplacement() {
    return delegate.getDollarReplacement();
  }

  @Override
  public void setDollarReplacement(String dollarReplacement) {
    delegate.setDollarReplacement(dollarReplacement);
  }

  @Override
  public String getEscapeCharReplacement() {
    return delegate.getEscapeCharReplacement();
  }

  @Override
  public void setEscapeCharReplacement(String escapeCharReplacement) {
    delegate.setEscapeCharReplacement(escapeCharReplacement);
  }

  @Override
  public boolean isAllowRedirects() {
    return delegate.isAllowRedirects();
  }

  @Override
  public void setAllowRedirects(boolean allowRedirects) {
    delegate.setAllowRedirects(allowRedirects);
  }

  @Override
  public boolean isOnDemandUpdateStatusChange() {
    return delegate.isOnDemandUpdateStatusChange();
  }

  @Override
  public void setOnDemandUpdateStatusChange(boolean onDemandUpdateStatusChange) {
    delegate.setOnDemandUpdateStatusChange(onDemandUpdateStatusChange);
  }

  @Override
  public String getEncoderName() {
    return delegate.getEncoderName();
  }

  @Override
  public void setEncoderName(String encoderName) {
    delegate.setEncoderName(encoderName);
  }

  @Override
  public String getDecoderName() {
    return delegate.getDecoderName();
  }

  @Override
  public void setDecoderName(String decoderName) {
    delegate.setDecoderName(decoderName);
  }

  @Override
  public String getClientDataAccept() {
    return delegate.getClientDataAccept();
  }

  @Override
  public void setClientDataAccept(String clientDataAccept) {
    delegate.setClientDataAccept(clientDataAccept);
  }

  @Override
  public boolean isShouldUnregisterOnShutdown() {
    return delegate.isShouldUnregisterOnShutdown();
  }

  @Override
  public void setShouldUnregisterOnShutdown(boolean shouldUnregisterOnShutdown) {
    delegate.setShouldUnregisterOnShutdown(shouldUnregisterOnShutdown);
  }

  @Override
  public boolean isShouldEnforceRegistrationAtInit() {
    return delegate.isShouldEnforceRegistrationAtInit();
  }

  @Override
  public void setShouldEnforceRegistrationAtInit(boolean shouldEnforceRegistrationAtInit) {
    delegate.setShouldEnforceRegistrationAtInit(shouldEnforceRegistrationAtInit);
  }

  @Override
  public boolean equals(Object o) {
    return delegate.equals(o);
  }

  @Override
  public int hashCode() {
    return delegate.hashCode();
  }

  @Override
  public String toString() {
    return delegate.toString();
  }

  private void cleanUrl(Map<String, String> serviceUrl){
    for (Map.Entry<String, String> entry : serviceUrl.entrySet()) {
      entry.setValue(StringUtils.deleteWhitespace(entry.getValue()));
    }
  }
}
