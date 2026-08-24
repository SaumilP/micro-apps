package org.sandcastle.apps;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class Project {
    @JsonProperty("id")
    private UUID id;

    @JsonProperty("userId")
    private String userId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    public Project() {}

    public Project(UUID id, String userId, String name, String description) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.description = description;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
