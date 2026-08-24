package org.sandcastle.apps;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@Path("/api/projects")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProjectResource {

    @GET
    public Uni<List<Project>> list(@QueryParam("userId") String userId) {
        if (userId != null) {
            return Project.find("userId", userId).list();
        }
        return Project.listAll();
    }

    @GET
    @Path("/{id}")
    public Uni<Response> get(@PathParam("id") UUID id) {
        return Project.<Project>findById(id)
                .map(project -> project != null
                    ? Response.ok(project).build()
                    : Response.status(Response.Status.NOT_FOUND).build());
    }

    @POST
    public Uni<Project> create(Project project) {
        return Panache.withTransaction(project::persist)
                .replaceWith(project);
    }

    @DELETE
    @Path("/{id}")
    public Uni<Response> delete(@PathParam("id") UUID id) {
        return Panache.withTransaction(() -> Project.deleteById(id))
                .map(deleted -> deleted
                    ? Response.noContent().build()
                    : Response.status(Response.Status.NOT_FOUND).build());
    }
}
