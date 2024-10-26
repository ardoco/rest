# ArDoCo REST
Details concerning the architectural decisions as well as the API responses can be found [here](architecture_decisions.md).

Remark: There also exists another branch (`extra-result-controller`) which introduces a new controller to handle the retrieval of the TraceLinks separate
from starting the pipeline. The branch also contains a ``.md`` file explaining the approach further.

## how to run the API

1. The API uses a Redis database. Add the credentials to ``redis/redis_template.env`` and rename the file to ``redis/redis.env``
2. Start redis in docker from the docker-compose.yaml (``docker-compose up -d``)
3. Start the application 
4. To access the API through the ui provided by swagger, go to http://localhost:8080/swagger-ui/index.html
5. The contents of the database can be accessed through http://localhost:5540