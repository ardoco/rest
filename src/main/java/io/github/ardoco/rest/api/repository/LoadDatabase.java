//package io.github.ardoco.rest.api.repository;
//
//import io.github.ardoco.rest.api.entity.ArDoCoResultEntity;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class LoadDatabase {
//    private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);
//
//    /**
//     * requests a copy of the EmployeeRepository
//     * creates 2 entities and stores them
//     */
//    @Bean
//    CommandLineRunner initDatabase(ArDoCoResultEntityRepository resultRepository) { // spring boot runs all command line runners, once the application context is loaded
//
//        return args -> {
//            log.info("Preloading{}", resultRepository.save(new ArDoCoResultEntity()));
//
//            resultRepository.findAll().forEach(employee -> log.info("Preloaded " + employee));
//
//        };
//    }
//}
//
