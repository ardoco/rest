# API
### Useful links:
System health check: http://localhost:8080/actuator/health

Swagger UI with API endpoints: http://localhost:8080/swagger-ui/index.htm

Redis insight: http://localhost:5540/


# Main Architecture Decisions of the REST API

## Controller

### Purpose
This is the outermost layer of the REST API and is responsible for the HTTP input and forwarding it to the service layer.

### Remarks
- So far, the API doesn't allow users to define additional Configs (in the Controller classes)
This is because at the time of implementation, these configs (which can be used to define the pipeline in the 
ArDoCoForSadCodeTraceabilityLinkRecovery) are not used. 
They can be added later as param in the methods of the controller.

### Accepted file types:
- So far no file checks have been implemented and is left to ardoco itself. It is only checked whether the
file is empty or not.

## Service
### Purpose:
This layer is responsible for processing the input and making the needed calls to ArDoCo to get a result.

### Remarks

- The output directory, which is required by ardoco when running any pipeline, is internally set to a temporary directory 
and is not made available to the outside, since the result will be returned in form of a response entity

- only the direct interaction with ardoco is asynchronous. Handling the input file (including conversion and 
checking whether its file type is correct) is done before, since like this the user can get quicker feedback that
sth. went wrong.

- The ids of the ongoing asynchronous calls are stored in a concurrentHashmap. This has the advantage that
when a user calls getResult to potentially receive the result, it can first be checked in the concurrentHashmap whether
the asynchronous call of ardoco has finished yet instead of unnecessarily doing a database call. 
Additionally storing the Completable Futures in the hashmap allows wait for ardoco without constantly
querying the database for a result.

## Hashing (Generating the ProjectID)
Only the files are used to create the hash, the configs not, meaning that in case only the configs change, the same
hash is generated. In the future, the configs might need to be hashed as well.
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
by the classes which use the database. 

## API Response schemas
The API has 2 response schemas: 
1. **Schema for expected behaviour** \
    - #### Sad-Code
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
    - #### Sad-Sam
    - #### Sad-Sam-Code
       Example: 
```json
{
  "projectId": "SadCodeResult:bigbluebutton67C34469A21A66DC94FD39531C9C1E6C",
  "status": "OK",
  "message": "The result is ready.",
  "samSadTraceLinks": [
    [
      "presentations",
      "FileTypeConstants"
    ],
    [
      "BigBlueButton",
      "MeetingsResponse"
    ]]
}
   ```
Note: Depending on the invoked endpoint and on the concrete result, some parameters (esp traceLinks) might be null

2. Schema for when an error occurred
Example:
```json
{
  "timestamp": "18-09-2024 20:45:13",
  "status": "UNPROCESSABLE_ENTITY",
  "message": "File not found."
}
```
Note: Exceptions are centrally handled by the GlobalExceptionHandler which produces a such an Error message for 
the user in case an exception is thrown which is not caught elsewhere. This central handling of exceptions standartizes
the way how the system deals with errors.