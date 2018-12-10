package com.base.components.common.configuration;

import brave.sampler.Sampler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.sleuth.sampler.ProbabilityBasedSampler;
import org.springframework.cloud.sleuth.sampler.SamplerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SleuthConfiguration
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-03-06 13:29
 */
@Configuration
@ConditionalOnClass(name = "org.springframework.cloud.sleuth.sampler.SamplerProperties")
@EnableConfigurationProperties(SamplerProperties.class)
public class SleuthConfiguration {
  @Bean
  public Sampler defaultSampler(SamplerProperties samplerProperties) {
    return new ProbabilityBasedSampler(samplerProperties);
  }
}
