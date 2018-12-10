/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.elasticsearch.configuration;

import com.base.components.common.boot.Profiles;
import com.base.components.common.constants.InvasiveRewrite;
import com.base.components.common.elasticsearch.EsIndex;
import com.base.components.common.service.database.DatabaseExecutor;
import com.base.components.elasticsearch.repository.base.NoneFacetedAggregatedPage;
import com.base.components.elasticsearch.repository.base.impl.GenericElasticsearchRepositoryImpl;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchRepositoriesAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.DefaultResultMapper;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.convert.MappingElasticsearchConverter;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

//@EnableElasticsearchRepositories(value = "com.mj.he800.elasticsearch.repository", repositoryBaseClass = GenericElasticsearchRepositoryImpl.class)
@Configuration
@ConditionalOnProperty(value = "base.elasticsearch.enable", havingValue = "true")
@ImportAutoConfiguration({ElasticsearchDataAutoConfiguration.class, ElasticsearchRepositoriesAutoConfiguration.class})
public class BaseElasticSearchConfig implements DisposableBean {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /**
   * Elastic Search 模块需要依赖数据库 Database 模块
   */
  @Autowired
  private DatabaseExecutor databaseExecutor;

  @Value("${spring.data.elasticsearch.properties.transport-addresses}")
  private String addresses;

  @Value("${spring.data.elasticsearch.properties.transport-addresses-split}")
  private String split;

  @Value("${spring.data.elasticsearch.cluster-name:elasticsearch}")
  private String clusterName;

  @Value("${info.app.project}")
  private String projectName;

  private Client client;

  @PostConstruct
  public void init(){
    GenericElasticsearchRepositoryImpl.setDatabaseExecutor(databaseExecutor);
  }

  @Bean
  @InvasiveRewrite
  public ElasticsearchTemplate elasticsearchTemplate() {
    MappingElasticsearchConverter converter = new MappingElasticsearchConverter(
      new SimpleElasticsearchMappingContext());
    DefaultResultMapper mapper = new DefaultResultMapper(converter.getMappingContext()) {
      @Override
      public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {
        AggregatedPage<T> ts = super.mapResults(response, clazz, pageable);
        if(response.getAggregations() != null && response.getScrollId() != null){
          return ts;
        }
        else{
          return new NoneFacetedAggregatedPage<>(ts.getContent(), pageable, ts.getTotalElements());
        }
      }
    };
    return new ElasticsearchTemplate(client, converter, mapper);
  }

  @Bean
  public Client elasticsearchClient() throws IOException {
    Settings settings = Settings.builder().put("cluster.name", clusterName)
                                .put("client.transport.sniff", false)
                                .build();

    TransportClient client = new PreBuiltTransportClient(settings);
    List<TransportAddress> addressList = Lists.newArrayList();
    for (String address : StringUtils.split(addresses, split)) {
      String[] s = StringUtils.split(address, ":");
      TransportAddress transportAddress = new InetSocketTransportAddress(
        new InetSocketAddress(s[0], Integer.valueOf(s[1])));
      addressList.add(transportAddress);
    }
    Assert.notEmpty(addressList, "ElasticSearch client address is empty !");
    client.addTransportAddresses(addressList.toArray(new TransportAddress[addressList.size()]));
    this.client = client;
    return client;
  }

  @Bean
  public EsIndex esIndex() {
    Profiles profile = Profiles.getProfiles();
    return new EsIndex() {
      @Override
      public String getIndexProfile() {
        return profile == null ? "" : profile.toString().toLowerCase();
      }
      @Override
      public String getProjectName() {
        return projectName;
      }
    };
  }

  private boolean isRealHost(InetSocketAddress address) {
    Socket socket = new Socket();
    try {
      socket.connect(address, 3000);
    } catch (IOException e) {
      return false;
    } finally {
      try {
        socket.close();
      } catch (IOException ignored) {
      }
    }
    return true;
  }

  @Override
  public void destroy() throws Exception {
    if (this.client != null) {
      try {
        if (logger.isInfoEnabled()) {
          logger.info("Closing Elasticsearch client");
        }
        this.client.close();
      } catch (final Exception ex) {
        if (logger.isErrorEnabled()) {
          logger.error("Error closing Elasticsearch client: ", ex);
        }
      }
    }
  }
}
