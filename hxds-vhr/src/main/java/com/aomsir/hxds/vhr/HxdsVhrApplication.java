package com.aomsir.hxds.vhr;

import com.codingapi.txlcn.tc.config.EnableDistributedTransaction;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

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
@MapperScan("com.aomsir.hxds.vhr.db.dao")
@ComponentScan("com.aomsir.*")
@EnableDistributedTransaction
public class HxdsVhrApplication {

    public static void main(String[] args) {
        SpringApplication.run(HxdsVhrApplication.class, args);
    }

}
