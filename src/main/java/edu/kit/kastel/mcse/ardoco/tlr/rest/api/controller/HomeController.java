/* Licensed under MIT 2024-2025. */
package edu.kit.kastel.mcse.ardoco.tlr.rest.api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * This controller redirects the root URL to the Swagger UI.
 */
@Controller
public class HomeController {

    /** Constructor for HomeController. */
    public HomeController() {
        // Default constructor
    }

    /**
     * Redirects the root URL to the Swagger UI.
     *
     * @return a redirect to the Swagger UI index page
     */
    @GetMapping("/")
    public String redirectToSwagger() {
        return "redirect:/swagger-ui/index.html";
    }
}
