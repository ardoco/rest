import io.github.ardoco.rest.ArDoCoRestApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(
        classes = ArDoCoRestApplication.class,
        webEnvironment = WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
abstract class AbstractIntegrationTest {

    static GenericContainer<?> redis =
            new GenericContainer<>(DockerImageName.parse("redis:7.4.0-alpine")).withExposedPorts(6379);

    protected static final Logger logger = LoggerFactory.getLogger(AbstractIntegrationTest.class);


    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        Slf4jLogConsumer logConsumer = new Slf4jLogConsumer(logger);
        redis.start();
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", redis::getFirstMappedPort);
        redis.followOutput(logConsumer);
    }
}