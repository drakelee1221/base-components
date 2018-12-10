/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.feign.configuration;

import com.base.components.common.boot.SpringBootApplicationRunner;
import com.base.components.common.rpc.ribbon.CustomRibbonConfiguration;
import com.base.components.feign.external.ExternalFeignClientRegistry;
import com.base.components.feign.header.DynamicHeaderRegistry;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.ribbon.RibbonClients;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.Map;

/**
 * Feign Configuration
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2017-11-30 15:29
 */
//@EnableFeignClients(basePackages = {"com.mj.he800.feign.client.**"})
@Configuration
@EnableCircuitBreaker
@ImportAutoConfiguration(FeignClientsConfiguration.class)
@AutoConfigureAfter(value = FeignClientsConfiguration.class)
@RibbonClients(defaultConfiguration = CustomRibbonConfiguration.class)
@Import(ExternalFeignClientRegistry.class)
@Order(Ordered.LOWEST_PRECEDENCE + 100)
public class BaseFeignConfiguration {
  @Bean
  public ErrorDecoder errorDecoder() {
    return new AbstractErrorDecoderHandler.DefaultHandler();
  }
  /**
   * 单个服务的重试禁用，由交Ribbon控制
   * <p>
   * 参照 {@link FeignClientsConfiguration#feignRetryer()}
   * </p>
   */
  @Bean
  @ConditionalOnMissingBean
  public Retryer feignRetryer() {
    return Retryer.NEVER_RETRY;
  }

  @Configuration
  @ConditionalOnProperty(name = "feign.hystrix.enabled", havingValue = "true")
  public class FeignHystrixEnableConfiguration {

    @Bean
    @ConditionalOnProperty(name = "hystrix.command.default.execution.isolation.strategy", havingValue = "THREAD", matchIfMissing = true)
    public FeignClientDynamicHystrixConcurrencyStrategy dynamicHeaderHystrixConcurrencyStrategy() {
      return new FeignClientDynamicHystrixConcurrencyStrategy(SpringBootApplicationRunner.getProjectPackagePrefix());
    }

    @Bean
    @ConditionalOnProperty(name = "hystrix.command.default.execution.isolation.strategy", havingValue = "THREAD", matchIfMissing = true)
    public RequestInterceptor hystrixThreadHeaderInterceptor() {
      return build(true);
    }

    @Bean
    @ConditionalOnProperty(name = "hystrix.command.default.execution.isolation.strategy", havingValue = "SEMAPHORE")
    public RequestInterceptor hystrixSemaphoreHeaderInterceptor() {
      return build(false);
    }
  }


  /**
   * normal feign token header setter
   */
  @Bean
  @ConditionalOnProperty(name = "feign.hystrix.enabled", havingValue = "false", matchIfMissing = true)
  public RequestInterceptor normalHeaderInterceptor() {
    return build(false);
  }


  private static RequestInterceptor build(boolean threadStrategy) {
    if (threadStrategy) {
      return new RequestInterceptor() {
        @Override
        public void apply(RequestTemplate template) {
          //hystrix子线程
          for (Map.Entry<String, String> entry : FeignClientDynamicHystrixConcurrencyStrategy.FEIGN_CLIENT_HEADERS_THREAD_LOCAL
            .get().entrySet()) {
            template.header(entry.getKey(), entry.getValue());
          }
        }
      };
    } else {
      return new RequestInterceptor() {
        @Override
        public void apply(RequestTemplate template) {
          //主线程
          for (Map.Entry<String, String> entry : DynamicHeaderRegistry.getAllHeaders(null).entrySet()) {
            template.header(entry.getKey(), entry.getValue());
          }
        }
      };
    }
  }
}
