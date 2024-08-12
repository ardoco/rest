package io.github.ardoco.rest.api.repository;

import io.github.ardoco.rest.api.entity.ArDoCoResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;

interface ArDoCoResultRepository extends JpaRepository<ArDoCoResultEntity, Long> {
}
