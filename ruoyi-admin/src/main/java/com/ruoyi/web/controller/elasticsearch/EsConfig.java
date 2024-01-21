package com.ruoyi.web.controller.elasticsearch;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
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

    /**
     * 创建ES客户端部分
     * @return
     */
    @Bean
    public RestHighLevelClient esClient(){
        // 该方法接收一个RequestConfig.Builder对象，对该对象进行修改后然后返回。
        RestHighLevelClient highLevelClient = new RestHighLevelClient(
                RestClient.builder(new HttpHost(uri, port))
                        .setRequestConfigCallback(requestConfigBuilder -> {
                            /**
                             * 连接超时（默认为1秒）
                             * 套接字超时（默认为30秒）
                             * 更改客户端的超时限制默认30秒现在改为100*1000分钟
                             * 调整最大重试超时时间（默认为30秒）
                             */
                            return requestConfigBuilder.setConnectTimeout(5000 * 1000)
                                    .setSocketTimeout(6000 * 1000);
                        }));
        return highLevelClient;
    }

}
