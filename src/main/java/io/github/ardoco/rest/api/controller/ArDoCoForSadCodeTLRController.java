package io.github.ardoco.rest.api.controller;

//import io.github.ardoco.rest.api.repository.ArDoCoResultEntityRepository;
import io.github.ardoco.rest.api.service.ArDoCoForSadCodeTLRService;
import io.github.ardoco.rest.api.service.RunnerTLRService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

@RestController
public class ArDoCoForSadCodeTLRController {

    private final RunnerTLRService sadSamCodeTLRService;

    public ArDoCoForSadCodeTLRController(ArDoCoForSadCodeTLRService sadSamCodeTLRService) {
        this.sadSamCodeTLRService = sadSamCodeTLRService;
    }


    @PostMapping("/api/sad/code/start")
    public ResponseEntity<?> runPipeline(@RequestBody String projectName, @RequestParam("inputText") MultipartFile inputText, @RequestParam("inputCode") MultipartFile inputCode) {
        try {
            //right now: no additional configs, they can later be added to the mapping as parameter.
            SortedMap<String, String> additionalConfigs = new TreeMap<>();
            String unique_id = sadSamCodeTLRService.runPipeline(projectName, inputText, inputCode, additionalConfigs);
            return ResponseEntity.ok(unique_id);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }


    @GetMapping("/api/sad/code/{id}")
    public ResponseEntity<String> getResult(@PathVariable("id") String id) {
        Optional<String> result = sadSamCodeTLRService.getResult(id);

        if (result.isEmpty()) {
            // If result is not ready, return a processing status
            return ResponseEntity.status(HttpStatus.ACCEPTED).body("Result is still being processed. Please try again later.");
        }

        // If result is present, return it
        return ResponseEntity.ok(result.get());
    }
}
