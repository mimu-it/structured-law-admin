package com.ruoyi.web.controller.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;

import org.elasticsearch.client.RestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author xiao.hu
 * @date 2023-12-25
 * @apiNote
 */
@Configuration
public class EsConfig {
    @Bean
    public ElasticsearchClient esClient(){
        // 创建ES客户端部分
        RestClient restClient = RestClient.builder(
                new HttpHost("localhost", 9200)).build();
        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper());
        return new ElasticsearchClient(transport);
    }
}
