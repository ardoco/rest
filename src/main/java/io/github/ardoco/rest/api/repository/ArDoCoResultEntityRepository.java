package io.github.ardoco.rest.api.repository;

import io.github.ardoco.rest.api.entity.ArDoCoResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArDoCoResultEntityRepository extends CrudRepository<ArDoCoResultEntity, String> {
}
