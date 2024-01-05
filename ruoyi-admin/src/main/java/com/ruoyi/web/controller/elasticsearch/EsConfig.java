package com.ruoyi.web.controller.elasticsearch;

import org.apache.http.HttpHost;

import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author xiao.hu
 * @date 2023-12-25
 * @apiNote
 */
@Configuration
public class EsConfig {

    @Value("${spring.elasticsearch.uri}")
    private String uri;

    @Value("${spring.elasticsearch.port}")
    private Integer port;

    @Bean
    public RestHighLevelClient esClient(){
        // 创建ES客户端部分
        RestClientBuilder restClientBuilder = RestClient.builder(new HttpHost(uri, port));
        return new RestHighLevelClient(restClientBuilder);
    }
}
