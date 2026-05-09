package com.example.devflowapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DevFlowApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(DevFlowApiApplication.class, args);
    }

}
