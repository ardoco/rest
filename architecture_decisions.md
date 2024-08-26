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
- Architecture Model: JSON-file

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

## Hashing
Only the files are used to create the hash, the configs not, meaning that in case only the configs change, the same
hash is generated. In the future, the configs might need to be hashed as well.
A md5 hash is used to ensure to get a hash space great enough to ensure that the probability of collisions is almost 0.

The hashes are used as keys in the database. Since entries are automatically deleted after 24h and the hash space is
large enough this should work fine since there are few enough entries being stored at once.

## Database
The no-sql database Redis is used. The results of the querying ardoco are stored like a in a giant hash table.
This means, that everything is stored as key-result(in JSON format). The key is identically with the hash used
to check whether the result has been calculated before to avoid calculating it again. All entries have a 
Time To Live of 24h, so that the database never gets to large because of stored results which are not needed anymore 
(because the client's request has been too long ago).