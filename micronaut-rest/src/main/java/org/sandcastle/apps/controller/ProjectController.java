package org.sandcastle.apps.controller;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import org.sandcastle.apps.domain.Project;
import org.sandcastle.apps.repository.ProjectRepository;

import java.util.List;
import java.util.UUID;

@Controller
public class ProjectController {

    private final ProjectRepository repository;

    public ProjectController(ProjectRepository repository) {
        this.repository = repository;
    }

    @Get("/health")
    public HttpResponse<String> health() {
        return HttpResponse.ok("OK");
    }

    @Get("/api/projects")
    public List<Project> list(@Nullable @QueryValue String userId) {
        if (userId != null) {
            return repository.findByUserId(userId);
        }
        return (List<Project>) repository.findAll();
    }

    @Get("/api/projects/{id}")
    public HttpResponse<Project> get(UUID id) {
        return repository.findById(id)
                .map(HttpResponse::ok)
                .orElse(HttpResponse.notFound());
    }

    @Post("/api/projects")
    public Project create(@Body Project project) {
        return repository.save(project);
    }

    @Delete("/api/projects/{id}")
    public HttpResponse<?> delete(UUID id) {
        repository.deleteById(id);
        return HttpResponse.noContent();
    }
}
