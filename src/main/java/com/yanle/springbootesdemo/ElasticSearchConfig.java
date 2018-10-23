package com.yanle.springbootesdemo;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author Le Yan
 * @Description todo
 * @date 2018/10/23 15:46
 */
@Configuration
public class ElasticSearchConfig {
    private static final Logger logger = LoggerFactory.getLogger(ElasticSearchConfig.class);

    @Value("${elasticsearch.cluster.name}")
    private String clusterName;

    @Value("${elasticsearch.pool}")
    private String poolSize;

    @Value("${elasticsearch.ip}")
    private String hostName;

    @Value("${elasticsearch.port}")
    private String port;


    @Bean
    public TransportClient init() {
        TransportClient transportClient = null;
        try {
            // 配置信息
            Settings esSetting = Settings.builder()
                    .put("cluster.name", clusterName)
                    //增加嗅探机制，找到ES集群
                    .put("client.transport.sniff", true)
                    //增加线程池个数为1
                    .put("thread_pool.search.size", Integer.parseInt(poolSize))
                    .build();

            transportClient = new PreBuiltTransportClient(esSetting);
            TransportAddress inetSocketTransportAddress = new TransportAddress(InetAddress.getByName(hostName),
                    Integer.valueOf(port));
            transportClient.addTransportAddresses(inetSocketTransportAddress);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            logger.info("初始化bean失败");
        }
        return transportClient;
    }
}
