package com.willowlabs.willowlabsnotifyservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WillowlabsNotifyServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(WillowlabsNotifyServiceApplication.class, args);
    }

}
