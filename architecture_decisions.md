# API
### Useful links:
System health check: http://localhost:8080/actuator/health

Swagger UI with API endpoints: http://localhost:8080/swagger-ui/index.html

Redis insight: http://localhost:5540/


# Main Architecture Decisions of the REST API

## Controller

This is the outermost layer of the REST API and is responsible for the HTTP input and forwarding it to the service layer.\
Each ardoco-runner is represented by one controller: sad-sam, sad-code, sam-code, sad-sam-code\
Each controller has 4 Endpoints:
- runPipeLine: starts the Ardoco Pipeline for the runner and returns a unique id which can be used to retreive the result
- runPipelineAndWait: starts the Ardoco Pipeline for the runner and waits up to 60 seconds for the result, otherwise it
  simply returns the id with which the result can be queried
- getResult: returns the result for the given id, if it already exisits
- waitForResult: waits up to 60 seconds for the result of the given id

The endpoints that start the pipeline each proceed in a similar way:
1. convert the inputMultipartFiles into Files
2. generate a unique id from the input files, the projectName and the runner/tracelink-type (sad-sam, sad-code,...) to
   later identify the result using an md5 hash
3. set up the runner using the input Files
4. forward the runner to the service layer, which runs the pipeline asynchronously in case no result is in the database yet,
   or which simply gets the result from the database
5. case runPipeline: return the unique id
6. case runPipelineAndWait: return unique id and result if present after 60 seconds

## Notes on the Architecture
Handling the result from calling the service is similar for all controllers. This similar behaviour can be found in the AbstractController class.\
Moreover are the endpoints to retrieve the results the same for each controller (meaning, you can also use the
getResult-endpoint from sad-sam to query the result from a sam-code pipeline). The ResultController allows to retrieve or 
wait for the result given an id

### Remarks
- So far, the API doesn't allow users to define additional Configs (in the Controller classes)
  This is because at the time of implementation, these configs (which can be used to define the pipeline in the
  ArDoCoForSadCodeTraceabilityLinkRecovery) are not used by ArDoCo.
  They can be added later as param in the methods of the controller.

### Accepted file types:
- So far no file checks have been implemented. This is left to ArDoCo itself. It is only checked whether the
  file is empty or not.

## Service
### Purpose:
This layer is responsible for processing the input and making the needed calls to ArDoCo to run the pipeline in
order to retrieve a result.

### Architectural Remarks
The Controllers already set up the runner. The controllers then feed the runner to the runPipeline() method. 

The controllers are organised into a 3 level inheritance structure, with AbstractService
as the parent, which contains the logic needed by all services. ResultService and AbstractRunnerTLRService inherit from it.
ResultService is responsible for the logic of getResult() and waitForResult, while AbstractRunnerTLRService contains the
logic for starting the pipelines. Each runner has its own inherited concrete service to ensure the ArDoCoResult is
handled correctly before it is stored.


The current way of doing things has 
the disadvantage that ArdoCo is already invoked in the controller and not only in the service layer and that the runner 
is always set up for the runPipeline-methods ignoring whether the result already in the database or not.\
Another option would be to only invoke ArDoCo in the service layer. This means that setting up the runner would be
needed to do there as well. But since the setup-methods of the runners each require different parameters, there can't be
a unified interface containing the startPipeline() methods without introducing a lot of complexity through generics.

### Remarks

- The ids of the ongoing asynchronous calls are stored in a concurrentHashmap. This has the advantage that
  when a user calls getResult to potentially receive the result, it can first be checked in the concurrentHashmap whether
  the asynchronous call of ardoco has finished yet instead of unnecessarily doing a database call.
  Additionally storing the Completable Futures in the hashmap allows to wait for the ArDoCoResult without constantly
  querying the database for a result.

## Remarks to Interacting with ArDoCo

- The output directory, which is required by ardoco when running any pipeline, is internally set to a temporary directory
  and is not made available to the outside, since the result will be returned in form of a response entity

- only the direct interaction with ardoco is asynchronous. Handling the input file (including conversion and
  checking whether its file type is correct) is done before, since like this the user can get quicker feedback that
  something went wrong.

## Hashing (Generating the ProjectID)
Only the files, the projectName and the controller/traceLink-type are used to create the hash, the configs not, meaning
that in case only the configs change, the same hash is generated. In the future, the configs might need to be hashed as well.
A md5 hash is used to ensure to get a hash space great enough to ensure that the probability of collisions is almost 0.

The hashes are used as keys in the database. Since entries are automatically deleted after 24h and the hash space is
large enough this should work fine since there are few enough entries being stored at once.

## Database (Repository Layer)
The no-sql database Redis is used. The results of the querying ardoco are stored like a in a giant hash table.
This means, that everything is stored as key-result(in JSON format). The key is identically with the hash used
to check whether the result has been calculated before to avoid calculating it again. All entries have a
Time To Live of 24h, so that the database never gets to large because of stored results which are not needed anymore
(because the client's request has been too long ago).

To be able to change the database used smoothly, repositories implement a DatabaseAccessor Interface, which is used
by the classes which use the database (e.g the Services).

## Converting the Tracelinks to JSON
The found traceLinks are converted into a raw JSON-String directly after the pipeline has finished and stored in the database as raw JSON,
to avoid having to convert the result multiple times in case a user queries the ready result multiple times.
The TraceLinkConverter-Class provides functionality to convert different types of traceLink into JSON.

## Exception Handling
Exceptions are centrally handled by the GlobalExceptionHandler which produces a such an ErrorResponse for
the user in case an exception is thrown which is not caught elsewhere. This central handling of exceptions standardizes
the way how the system deals with errors.

## API Response schemas
The API has 2 response schemas:
1. **Schema for expected behaviour** \
    - #### Sad-Code
      Example:
   ```json
   {
        "requestId": "SadCodeResult:bigBlueButtonF2BD94533508F2F2DE4130AB43403B63",
        "status": "OK",
        "message": "The result is ready.",
        "traceLinkType": "SAD_CODE",
        "traceLinks": [
        {
        "sentenceNumber": 56,
        "codeCompilationUnit": "bbb-fsesl-client/src/main/java/org/freeswitch/esl/client/internal/debug/ExecutionHandler.java"
        },
        {
        "sentenceNumber": 57,
        "codeCompilationUnit": "bbb-fsesl-client/src/main/java/org/freeswitch/esl/client/internal/debug/ExecutionHandler.java"
        }]
   }
   ```
    - #### Sam-Code
      Example:
    ```json
    {
        "requestId": "SamCodeResult:bigBlueButton2B867FE03AF1FE8DE3C1DEE7F1D9CB4E",
        "status": "OK",
        "message": "The result is ready.",
        "traceLinkType": "SAM_CODE",
        "traceLinks": [
        {
        "modelElementId": "_9wZIcFkHEeyewPSmlgszyA",
        "modelElementName": "FSESL",
        "codeElementId": "acm005843jsd",
        "codeElementName": "bbb-fsesl-client/src/main/java/org/freeswitch/esl/client/manager/DefaultManagerConnection.java"
        },
        {
        "modelElementId": "_nwrCMFwPEeyiuNx_RO7j-Q",
        "modelElementName": "FreeSWITCH",
        "codeElementId": "acm005938jsd",
        "codeElementName": "bbb-fsesl-client/src/main/java/org/freeswitch/esl/client/outbound/example/SimpleHangupPipelineFactory.java"
        }]
    }
   ```
    - #### Sad-Sam
      Example:
   ```json
    {
        "requestId": "SadSamResult:bigBlueButton6AA76050BA630F6D8A6E099A30D1053C",
        "status": "OK",
        "message": "The result is ready.",
        "traceLinkType": "SAD_SAM",
        "traceLinks": [
        {
        "sentenceNumber": 3,
        "modelElementUid": "_0e5u8FkHEeyewPSmlgszyA",
        "confidence": 1
        },
        {
        "sentenceNumber": 4,
        "modelElementUid": "_s0aIcFkHEeyewPSmlgszyA",
        "confidence": 0.8
        }]
    }
    ```
    - #### Sad-Sam-Code
      Example:
```json
{
  "requestId": "SadSamCodeResult:bigBlueButton8E0E764E3B368781CF0DDDC67F19ABC0",
  "status": "OK",
  "message": "The result is ready.",
  "traceLinkType": "SAD_SAM_CODE",
  "traceLinks": [
    {
      "sentenceNumber": 25,
      "codeCompilationUnit": "akka-bbb-apps/src/main/scala/org/bigbluebutton/core/util/jhotdraw/PathData.java"
    },
    {
      "sentenceNumber": 49,
      "codeCompilationUnit": "akka-bbb-apps/src/main/scala/org/bigbluebutton/core/util/jhotdraw/PathData.java"
    }]
}
   ```
Note: Depending on the invoked endpoint and on the concrete result, some parameters (esp traceLinks) might be null

2. **Schema for when an error occurred**
   Example:
```json
{
  "timestamp": "09-10-2024 12:58:42",
  "status": "UNPROCESSABLE_ENTITY",
  "message": "No result with key randomID123 found."
}
```