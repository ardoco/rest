package io.github.ardoco.rest.api.service;

//import io.github.ardoco.rest.api.entity.ArDoCoResultEntity;
// import io.github.ardoco.rest.api.repository.ArDoCoResultEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.SortedMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public abstract class RunnerTLRService {

//    protected final ArDoCoResultEntityRepository arDoCoResultRepository;

//    public RunnerTLRService(ArDoCoResultEntityRepository repository){
//        this.arDoCoResultRepository = repository;
//    }

    @Autowired
    protected RedisTemplate<String, String> template;

    public RunnerTLRService() {}

    abstract public String runPipeline(String projectName, MultipartFile inputText, MultipartFile inputCode, SortedMap<String, String> additionalConfigs) throws Exception;

    abstract public Optional<String> getResult(String id);

    abstract public String waitForResult(String id) throws ExecutionException, InterruptedException;

    abstract public String runPipelineAndWaitForResult(String projectName, MultipartFile inputText, MultipartFile inputCode, SortedMap<String, String> additionalConfigs) throws Exception;

//    protected String saveResult(ArDoCoResultEntity resultEntity) {
//        ArDoCoResultEntity savedEntity = arDoCoResultRepository.save(resultEntity);
//        return savedEntity.getId();
//    }

    /**
     * Stores a jsonResult in the database for 24h, using id as key to retrieve the result later
     *
     * @param id the key that should be used to retrieve the jsonResult from the database
     * @param jsonResult the value that should be saved to the redis database
     * @return Return the generated id which can be used to retrieve the result
     */
    protected String saveResult(String id, String jsonResult) {
        template.opsForValue().set(id, jsonResult, 24, TimeUnit.HOURS);
        return id;
    }


}
