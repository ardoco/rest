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
and is not made available to the outside, since the result will be returned in form of a response enitity