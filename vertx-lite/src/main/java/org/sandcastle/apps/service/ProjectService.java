package org.sandcastle.apps.service;

import io.vertx.core.Future;

import java.util.Optional;

import org.sandcastle.apps.dto.ProjectDto;
import org.sandcastle.apps.dto.ProjectsList;

public interface ProjectService {

    Future<ProjectDto> createProject(ProjectDto projectDTO);

    Future<ProjectDto> updateProject(ProjectDto projectDTO);

    Future<Optional<ProjectDto>> findProjectById(Integer id);

    Future<Void> removeProject(Integer id);

    Future<ProjectsList> findProjectsByUser(Integer userId);
}
