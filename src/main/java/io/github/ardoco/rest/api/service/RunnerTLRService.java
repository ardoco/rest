package io.github.ardoco.rest.api.service;

import io.github.ardoco.rest.api.entity.ArDoCoResultEntity;
import io.github.ardoco.rest.api.repository.ArDoCoResultEntityRepository;
import org.springframework.web.multipart.MultipartFile;

import java.util.SortedMap;

public abstract class RunnerTLRService {

    protected final ArDoCoResultEntityRepository arDoCoResultRepository;

    public RunnerTLRService(ArDoCoResultEntityRepository repository){
        this.arDoCoResultRepository = repository;
    }

    abstract public long runPipeline(String projectName, MultipartFile inputText, MultipartFile inputCode, SortedMap<String, String> additionalConfigs) throws Exception;

    abstract public String getResult(long id);


    /**
     *
     * @param resultEntity
     * @return Return the generated ID as the UID
     */
    protected Long saveResult(ArDoCoResultEntity resultEntity) {
        ArDoCoResultEntity savedEntity = arDoCoResultRepository.save(resultEntity);
        return savedEntity.getId();
    }
}
