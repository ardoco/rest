package io.github.ardoco.rest.api.controller;

import io.github.ardoco.rest.api.service.ArDoCoForSadCodeTLRService;
import io.github.ardoco.rest.api.service.runnerTLRService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.SortedMap;
import java.util.TreeMap;

@RestController
public class ArDoCoForSadCodeTLRController {

    private final runnerTLRService sadSamCodeTLRService;

    ArDoCoForSadCodeTLRController(ArDoCoForSadCodeTLRService sadSamCodeTLRService) {
        this.sadSamCodeTLRService = sadSamCodeTLRService;
    }

    @PostMapping("/api/sad/code/start")
    public ResponseEntity<?> runPipeline(@RequestBody String projectName, @RequestParam("inputText") MultipartFile inputText, @RequestParam("inputCode") MultipartFile inputCode) {
        try {
            //right now: no additional configs, they can later be added to the mapping as parameter.
            SortedMap<String, String> additionalConfigs = new TreeMap<>();
            sadSamCodeTLRService.runPipeline(projectName, inputText, inputCode, additionalConfigs);
        } catch (Exception e) {
            // TODO: return ResponseEnitiy with error message
        }

        return ResponseEntity.ok("Successfully uploaded the file");
    }
}
