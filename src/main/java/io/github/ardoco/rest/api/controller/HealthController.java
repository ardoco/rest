package io.github.ardoco.rest.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping("/")
    public ResponseEntity<String> checkHealth() {
        // You can perform additional checks here (e.g., database, external services, etc.)
        return new ResponseEntity<>("Server is up", HttpStatus.OK);
    }
}
