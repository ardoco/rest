/* Licensed under MIT 2025-2026. */
package edu.kit.kastel.mcse.ardoco.tlr.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import edu.kit.kastel.mcse.ardoco.tlr.rest.api.converter.ModelFormatTypeConverter;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WebConfigCorsTest {

    private static final String ALLOWED_ORIGIN = "https://ardoco.de";
    private static final String FOREIGN_ORIGIN = "https://evil.example";
    private static final String ENDPOINT = "/api/ardocode/start";

    @Test
    void preflightFromAllowedOriginIsAccepted() throws Exception {
        MockMvc mockMvc = createMockMvc(ALLOWED_ORIGIN);
        mockMvc.perform(preflight(ALLOWED_ORIGIN))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGIN))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET,POST"))
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
    }

    @Test
    void preflightFromForeignOriginIsRejected() throws Exception {
        MockMvc mockMvc = createMockMvc(ALLOWED_ORIGIN);
        mockMvc.perform(preflight(FOREIGN_ORIGIN)).andExpect(status().isForbidden()).andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void preflightWithDisallowedMethodIsRejected() throws Exception {
        MockMvc mockMvc = createMockMvc(ALLOWED_ORIGIN);
        mockMvc.perform(options(ENDPOINT).header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN).header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "DELETE"))
                .andExpect(status().isForbidden());
    }

    @Test
    void noOriginsConfiguredDisablesCors() throws Exception {
        MockMvc mockMvc = createMockMvc("");
        mockMvc.perform(preflight(ALLOWED_ORIGIN)).andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    private RequestBuilder preflight(String origin) {
        return options(ENDPOINT).header(HttpHeaders.ORIGIN, origin).header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.POST.name());
    }

    private MockMvc createMockMvc(String origins) {
        try (AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext()) {
            context.setServletContext(new MockServletContext());
            TestPropertyValues.of("cors.allowed-origins=" + origins).applyTo(context);
            context.register(TestConfig.class);
            context.refresh();
            return MockMvcBuilders.webAppContextSetup(context).build();
        }
    }

    @Configuration
    @EnableWebMvc
    @Import({ WebConfig.class, ModelFormatTypeConverter.class, DummyController.class })
    private static class TestConfig {
    }

    @RestController
    @RequestMapping("/api/ardocode")
    private static final class DummyController {
        @PostMapping("/start")
        ResponseEntity<String> start() {
            return ResponseEntity.ok("started");
        }
    }
}
