package org.sandcastle.apps.repository;

import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import org.sandcastle.apps.domain.Project;

import java.util.List;
import java.util.UUID;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface ProjectRepository extends CrudRepository<Project, UUID> {

    List<Project> findByUserId(String userId);
}
