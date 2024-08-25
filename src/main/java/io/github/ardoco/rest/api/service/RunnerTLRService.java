package io.github.ardoco.rest.api.service;

//import io.github.ardoco.rest.api.entity.ArDoCoResultEntity;
// import io.github.ardoco.rest.api.repository.ArDoCoResultEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.SortedMap;
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


//    /**
//     *
//     * @param resultEntity the ArDoCoResult that should be saved
//     * @return Return the generated ID as the UID
//     */
//    protected String saveResult(ArDoCoResultEntity resultEntity) {
//        ArDoCoResultEntity savedEntity = arDoCoResultRepository.save(resultEntity);
//        return savedEntity.getId();
//    }

    protected String saveResult(String key, String jsonResult) {
        template.opsForValue().set(key, jsonResult, 24, TimeUnit.HOURS);
        return key;
    }


}
