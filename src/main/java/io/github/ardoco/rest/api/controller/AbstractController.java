//package io.github.ardoco.rest.api.controller;
//
//import io.github.ardoco.rest.api.api_response.ArdocoResultResponse;
//import io.github.ardoco.rest.api.api_response.ResultBag;
//import io.github.ardoco.rest.api.exception.*;
//import io.github.ardoco.rest.api.service.RunnerTLRService;
//import io.github.ardoco.rest.api.util.Messages;
//import io.swagger.v3.oas.annotations.Parameter;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.Optional;
//import java.util.SortedMap;
//import java.util.TreeMap;
//
//public abstract class AbstractController {
//
//
//    public AbstractTraceLinkController() {
//    }
//
//    // Common runPipeline logic
//    public ResponseEntity<ArdocoResultResponse> handleRunPipeLineResult(ResultBag result)
//            throws FileNotFoundException, FileConversionException, HashingException {
//
//        ArdocoResultResponse response;
//        if (result.traceLinks() != null) {
//            response = new ArdocoResultResponse(result.projectId(), HttpStatus.OK, result.traceLinks(), Messages.RESULT_IS_READY);
//        } else {
//            response = new ArdocoResultResponse(result.projectId(), HttpStatus.OK, Messages.RESULT_IS_BEING_PROCESSED);
//        }
//        return new ResponseEntity<>(response, response.getStatus());
//    }
//
//    // Common getResult logic
//    public ResponseEntity<ArdocoResultResponse> getResult(@PathVariable("id") String id)
//            throws ArdocoException, IllegalArgumentException {
//
//        Optional<String> result = service.getResult(id);
//        ArdocoResultResponse response;
//        if (result.isEmpty()) {
//            response = new ArdocoResultResponse(id, HttpStatus.ACCEPTED, Messages.RESULT_NOT_READY);
//        } else {
//            response = new ArdocoResultResponse(id, HttpStatus.OK, result.get(), Messages.RESULT_IS_READY);
//        }
//        return new ResponseEntity<>(response, response.getStatus());
//    }
//
//    // Common waitForResult logic
//    public ResponseEntity<ArdocoResultResponse> waitForResult(@PathVariable("id") String id)
//            throws ArdocoException, InterruptedException, IllegalArgumentException, TimeoutException {
//
//        ArdocoResultResponse response;
//        try {
//            String result = service.waitForResult(id);
//            response = new ArdocoResultResponse(id, HttpStatus.OK, result, Messages.RESULT_IS_READY);
//        } catch (TimeoutException e) {
//            response = new ArdocoResultResponse(id, HttpStatus.ACCEPTED, Messages.REQUEST_TIMED_OUT);
//        }
//        return new ResponseEntity<>(response, response.getStatus());
//    }
//}
