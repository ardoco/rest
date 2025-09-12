/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.rest.api.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * This controller provides a health check for the ArDoCo API.
 */
@Tag(name = "Health Check")
@RestController
@RequestMapping("/api")
public class HealthController {

    /**
     * Constructs a new {@code HealthController}.
     */
    public HealthController() {
        // Default constructor
    }

    /**
     * Endpoint to check the health of the API.
     *
     * @return a ResponseEntity with a status of OK if the API is healthy
     */
    @GetMapping("/health")
    public ResponseEntity<String> checkHealth() {
        return ResponseEntity.ok("OK");
    }
}
