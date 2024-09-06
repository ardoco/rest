# Getting Started

### Reference Documentation

For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/3.3.2/maven-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/3.3.2/maven-plugin/build-image.html)
* [Spring HATEOAS](https://docs.spring.io/spring-boot/docs/3.3.2/reference/htmlsingle/index.html#web.spring-hateoas)
* [Spring Web](https://docs.spring.io/spring-boot/docs/3.3.2/reference/htmlsingle/index.html#web)

### Guides

The following guides illustrate how to use some features concretely:

* [Building a Hypermedia-Driven RESTful Web Service](https://spring.io/guides/gs/rest-hateoas/)
* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)

### Maven Parent overrides

Due to Maven's design, elements are inherited from the parent POM to the project POM.
While most of the inheritance is fine, it also inherits unwanted elements like `<license>` and `<developers>` from the
parent.
To prevent this, the project POM contains empty overrides for these elements.
If you manually switch to a different parent and actually want the inheritance, you need to remove those overrides.


### Docker
Use `docker-compose up -d` to start the docker containers containing the redis database and redis insights. 
Make sure that docker is already running on your machine when executing the command.
The contents of the database can be accessed through http://localhost:5540 . 
To stop the container, run `docker-compose down`. 

connect to the server using redis-cli: `docker exec -it redis redis-cli`
Then, authenticate yourself with: `AUTH <password>`

### Access the API
To access the API through the ui provided by swagger, go to http://localhost:8080/swagger-ui/index.html

To generate the OpenAPI specification, go to http://localhost:8080/v3/api-docs