package io.github.ardoco.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;


@EnableAsync
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class ArDoCoRestApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArDoCoRestApplication.class, args);

    }

}
