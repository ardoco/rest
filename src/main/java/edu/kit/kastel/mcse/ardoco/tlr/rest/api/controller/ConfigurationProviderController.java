package edu.kit.kastel.mcse.ardoco.tlr.rest.api.controller;

import edu.kit.kastel.mcse.ardoco.tlr.rest.api.service.ConfigurationProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;


@RestController
@RequestMapping("/api")
public class ConfigurationProviderController {

    @Autowired
    private ConfigurationProviderService configurationProviderService;

    @GetMapping("/configuration")
    public ResponseEntity<Map<String, String>> getConfiguration() throws InvocationTargetException, InstantiationException, IllegalAccessException {
        Map<String, String> config = configurationProviderService.getDefaultConfiguration();
        return ResponseEntity.ok(config);
    }

}
