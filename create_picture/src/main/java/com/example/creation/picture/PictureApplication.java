package com.example.creation.picture;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import springfox.documentation.oas.annotations.EnableOpenApi;


/**
 * @author xmy
 * @date 2021/3/2 14:23
 */
@EnableTransactionManagement
@SpringBootApplication
@EnableOpenApi
@EnableDiscoveryClient
@EnableFeignClients("com.example.creation.commons.feign")
@ComponentScan(basePackages = {
        "com.example.creation.commons.config.feign",
        "com.example.creation.commons.handler",
        "com.example.creation.commons.config.redis",
        "com.example.creation.utils",
        "com.example.creation.picture"})
public class PictureApplication {

    public static void main(String[] args) {
        SpringApplication.run(PictureApplication.class, args);
    }
}
