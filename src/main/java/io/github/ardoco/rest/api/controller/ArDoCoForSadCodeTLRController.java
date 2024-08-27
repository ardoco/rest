package io.github.ardoco.rest.api.controller;

import io.github.ardoco.rest.api.service.RunnerTLRService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

@RestController
public class ArDoCoForSadCodeTLRController {

    private final RunnerTLRService runnerTLRService;

    private static final Logger logger = LogManager.getLogger(ArDoCoForSadCodeTLRController.class);

    public ArDoCoForSadCodeTLRController(@Qualifier("sadCodeTLRService") RunnerTLRService runnerTLRService) {
        this.runnerTLRService = runnerTLRService;
    }


    @PostMapping("/api/sad-code/start")
    public ResponseEntity<?> runPipeline(@RequestBody String projectName, @RequestParam("inputText") MultipartFile inputText, @RequestParam("inputCode") MultipartFile inputCode) {
        try {
            //right now: no additional configs, they can later be added to the mapping as parameter.
            SortedMap<String, String> additionalConfigs = new TreeMap<>();
            String unique_id = runnerTLRService.runPipeline(projectName, inputText, inputCode, additionalConfigs);
            return ResponseEntity.ok(unique_id);
        } catch (Exception e) {
            String message = "An error occurred while processing the files: ";
            logger.error(message, e);
            return ResponseEntity.status(400).body(e.getMessage()); //idk is that the correct error? it in case of yes, the error status of the waitForResultMethod should be adjusted as well for consitency
        }
    }

    /**
     * Queries whether the ArDoCoResult is already there. If yes, it returns the result, else it returns a message
     * saying that the result is not ready yet
     *
     * @param id The id of the result that should be queried
     * @return the result in JSON format if the result already exists, else a message saying that the result is not ready yet.
     */
    @GetMapping("/api/sad-code/{id}")
    public ResponseEntity<String> getResult(@PathVariable("id") String id) {
        Optional<String> result = runnerTLRService.getResult(id);

        if (result.isEmpty()) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body("Result is still being processed. Please try again later.");
        }
        return ResponseEntity.ok(result.get());
    }

    /**
     * Queries the ArDoCoResult and returns it. In case it is not ready yet, it performs busy-waiting, meaning it
     * waits until the result ready and returns it then
     *
     * @param id The id of the result that should be queried
     * @return the result in JSON format
     */
    @GetMapping("/api/sad-code/wait/{id}")
    public ResponseEntity<String> waitForResult(@PathVariable("id") String id) {
        try {
            String result = runnerTLRService.waitForResult(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            String message = "An error occurred while waiting for the result with ID: " + id;
            logger.error(message, e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while processing your request. Please try again later.");

    }

    /**
     * Starts the ardoco-pipline to get a sad-code result and waits until the result is obtained
     *
     * @param projectName The name of the project for which a ArDoCo should perform sadCode trace link recovery
     * @param inputText the documentation of the project
     * @param inputCode the code pf the project
     * @return the result of performing sadCode trace link recovery in Json format
     */
    @PostMapping("/api/sad-code/start-and-wait")
    public ResponseEntity<String> runPipelineAndWaitForResult(@RequestBody String projectName, @RequestParam("inputText") MultipartFile inputText, @RequestParam("inputCode") MultipartFile inputCode) {
        try {
            //right now: no additional configs, they can later be added to the mapping as parameter.
            SortedMap<String, String> additionalConfigs = new TreeMap<>();
            String result = runnerTLRService.runPipelineAndWaitForResult(projectName, inputText, inputCode, additionalConfigs);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            String message = "An error occurred while processing the project files: ";
            logger.error(message, e);
            return ResponseEntity.status(400).body(e.getMessage()); //idk is that the correct error? it in case of yes, the error status of the waitForResultMethod should be adjusted as well for consitency
        }
    }

}
