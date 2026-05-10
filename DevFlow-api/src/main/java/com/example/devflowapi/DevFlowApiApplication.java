package com.example.devflowapi;

import com.example.devflowapi.metrics.DevFlowMetrics;
import com.example.devflowapi.repository.EnvironmentRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DevFlowApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(DevFlowApiApplication.class, args);
    }
    @Bean
    ApplicationRunner initMetrics(DevFlowMetrics metrics,
                                  EnvironmentRepository repo) {
        return args -> metrics.registerActiveEnvironmentsGauge(repo);
    }

}
