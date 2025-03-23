package org.sandcastle.apps.mappers;

import java.util.function.Function;

import org.sandcastle.apps.dto.ProjectDto;
import org.sandcastle.apps.entity.Project;

public class ProjectEntityMapper implements Function<ProjectDto, Project> {
    @Override
    public Project apply(ProjectDto projectDto) {
        Project entity = new Project();
        entity.setId(projectDto.id());
        entity.setUserId(projectDto.userId());
        entity.setName(projectDto.name());
        return entity;
    }
}
