package edu.kit.kastel.mcse.ardoco.tlr.rest;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;


@OpenAPIDefinition(
        info =
                @Info(
                        title = "ArDoCo: Trace Link Recovery",
                        description = "provides functionality to run ArDoCoTLR and provide results"
                )
)
@EnableAsync
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class ArDoCoRestApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArDoCoRestApplication.class, args);

    }
}