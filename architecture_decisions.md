# Main Architecture Decisions of the REST API

## Controller
So far, the API doesn't allow users to define additional Configs (in the Controller classes)
This is because at the time of implementation, these configs (which can be used to define the pipeline in the 
ArDoCoForSadCodeTraceabilityLinkRecovery) are not used. 
They can be added later as param in the methods of the controller.