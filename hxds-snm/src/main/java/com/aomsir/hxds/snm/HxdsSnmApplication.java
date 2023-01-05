package com.aomsir.hxds.snm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @Author: Aomsir
 * @Date: 2023/1/4
 * @Description:
 * @Email: info@say521.cn
 * @GitHub: https://github.com/aomsir
 */



@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@ServletComponentScan
@ComponentScan("com.aomsir.*")
@EnableAsync
public class HxdsSnmApplication {

    public static void main(String[] args) {
        SpringApplication.run(HxdsSnmApplication.class, args);
    }

}
