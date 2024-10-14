package com.ziumks.bulkApiServer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableScheduling
@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
public class BulkApiServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BulkApiServerApplication.class, args);
    }

}
