package com.autobeauty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class AutoBeautyApplication {
    public static void main(String[] args) {
        SpringApplication.run(AutoBeautyApplication.class, args);
    }
}
