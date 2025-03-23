package org.sandcastle.apps.mappers;

import java.util.function.Function;

import org.sandcastle.apps.dto.ProjectDto;
import org.sandcastle.apps.entity.Project;

public class ProjectDtoMapper implements Function<Project, ProjectDto> {
    @Override
    public ProjectDto apply(Project project) {
        return new ProjectDto(project.getId(), project.getUserId(), project.getName());
    }
}
