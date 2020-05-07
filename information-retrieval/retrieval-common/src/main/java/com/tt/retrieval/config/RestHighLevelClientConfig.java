package com.tt.retrieval.config;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.client.*;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.settings.Settings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.apache.http.client.config.RequestConfig.Builder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author LuHongGang
 * @version 1.0
 * @date 2020/5/4 3:31
 */
@Component
@Slf4j
public class RestHighLevelClientConfig {

    @Value("${springboot.es.host}")
    private String host;
    @Value("${springboot.es.scheme}")
    private String scheme;
    @Value("${springboot.es.token}")
    private String token;
//       @Value("${springboot.es.connectAddress}")
//    private String[] connectAddress;
//        @Value("${springboot.es.charset}")
    private String charSet;
    @Value("${springboot.es.socketTimeOut}")
    private Integer socketTimeOut;
    @Value("${springboot.es.connectTimeOut}")
    private Integer connectTimeOut;
    @Value("${springboot.es.connectionRequestTimeout}")
    private Integer connectionRequestTimeout;

    @Value("${springboot.es.maxConnTotal}")
    private Integer maxConnTotal;

    @Bean
    public RestClientBuilder restClientBuilder() {
//        HttpHost[] hostsArray = Arrays.stream(connectAddress)
//                .map(this::makeHttpHost)
//                .filter(Objects::nonNull)
//                .toArray(HttpHost[]::new);
        log.info("连接参数初始化hostList:,scheme:,token:,charset:,{},{},{},{}",host,scheme,token,charSet);
        String[] hosts = host.split(",");
        HttpHost[] httpHosts = new HttpHost[hosts.length];
        for (int i = 0; i < hosts.length; i++) {
            String hostName = hosts[i];
            httpHosts[i] = new HttpHost(hostName.split(":")[0],Integer.parseInt(hostName.split(":")[1]),scheme);
        }
        RestClientBuilder restClientBuilder = RestClient.builder(
                httpHosts
        );

        Header[] defaultHeaders = new Header[]{
                new BasicHeader("Accept", "*/*"),
                new BasicHeader("Charset", charSet),
                //设置token 是为了安全 网关可以验证token来决定是否发起请求 我们这里只做象征性配置
                new BasicHeader("E_TOKEN", token)
        };
        restClientBuilder.setDefaultHeaders(defaultHeaders);

//        restClientBuilder.setHttpClientConfigCallback(new RestClientBuilder.RequestConfigCallback() {
//
//            @Override
//            public Builder customizeRequestConfig(Builder requestConfigBuilder) {
//                requestConfigBuilder.setConnectTimeout(connectTimeOut);
//                Builder builder = requestConfigBuilder.setSocketTimeout(socketTimeOut);
//                requestConfigBuilder.setConnectionRequestTimeout(connectTimeOut);
//                return requestConfigBuilder;
//            }
//        });

        // httpclient连接延时配置
        restClientBuilder.setRequestConfigCallback(requestConfigBuilder -> {
            requestConfigBuilder.setConnectTimeout(connectTimeOut);
            requestConfigBuilder.setSocketTimeout(socketTimeOut);
            // setConnectionRequestTimeout 的值需考虑重置
            requestConfigBuilder.setConnectionRequestTimeout(connectionRequestTimeout);
            return requestConfigBuilder;
        });

        // 异步httpclient连接数配置
        restClientBuilder.setHttpClientConfigCallback(httpAsyncClientBuilder -> {
            httpAsyncClientBuilder.setMaxConnTotal(maxConnTotal);
//            httpAsyncClientBuilder.setMaxConnPerRoute(50);
            return httpAsyncClientBuilder;
        });

        restClientBuilder.setFailureListener(new RestClient.FailureListener(){
            @Override
            public void onFailure(Node node) {
                log.info(" ################ 监听某个es节点失败 ################ ");
            }
        });
        restClientBuilder.setRequestConfigCallback(builder ->
                builder.setConnectTimeout(connectTimeOut).setSocketTimeout(socketTimeOut));
        return restClientBuilder;
    }

    @Bean
    public RestHighLevelClient restHighLevelClient(RestClientBuilder restClientBuilder) {
       return new RestHighLevelClient(restClientBuilder);
    }

}
