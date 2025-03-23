package org.sandcastle.apps.repository;

import io.vertx.core.Future;

import java.util.Optional;

import org.sandcastle.apps.dto.ProjectDto;
import org.sandcastle.apps.dto.ProjectsList;

public interface ProjectRepository {

    Future<ProjectDto> createProject(ProjectDto projectDto);

    Future<ProjectDto> updateProject(ProjectDto projectDto);

    Future<Optional<ProjectDto>> findProjectById(Integer id);

    Future<Void> removeProject(Integer id);

    Future<ProjectsList> findProjectByUser(Integer userId);
}
