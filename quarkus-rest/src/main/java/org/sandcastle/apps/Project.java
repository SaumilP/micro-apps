package org.sandcastle.apps;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "project")
public class Project extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public UUID id;

    @Column(name = "user_id")
    public String userId;

    @Column(nullable = false)
    public String name;

    public String description;

    public Project() {
    }

    public Project(String userId, String name, String description) {
        this.userId = userId;
        this.name = name;
        this.description = description;
    }
}
