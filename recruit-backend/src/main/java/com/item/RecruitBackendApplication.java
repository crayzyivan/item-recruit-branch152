package com.item;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.time.LocalDateTime;
import java.util.TimeZone;

@Slf4j
@EnableDiscoveryClient
@EnableConfigurationProperties
@EnableTransactionManagement
@ComponentScan(basePackages = {"com.item.iam", "com.item"})
@EnableFeignClients(basePackages = "com.item.service.client")
@SpringBootApplication(exclude = {ElasticsearchRestClientAutoConfiguration.class})
@EnableAsync
@EnableAspectJAutoProxy(exposeProxy = true)
public class RecruitBackendApplication {

    @Value("${application.timezone:UTC}")
    private String applicationTimeZone;

	public static void main(String[] args) {
		SpringApplication.run(RecruitBackendApplication.class, args);
	}

    @PostConstruct
    public void setDefault() {
        log.info("current datetime change before {}", LocalDateTime.now());
        TimeZone.setDefault(TimeZone.getTimeZone(applicationTimeZone));
        log.info("current datetime change after {}", LocalDateTime.now());
    }
}
