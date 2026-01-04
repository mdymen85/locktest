package com.mdymen85.locktest;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Data;
import lombok.Getter;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "date_created", nullable = false, updatable = false)
    private Instant dateCreated;

    @Column(nullable = false)
    private Boolean activated = false;

    // --- Lifecycle hook to match DEFAULT CURRENT_TIMESTAMP ---
    @PrePersist
    protected void onCreate() {
        this.dateCreated = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "User{" +
               "id=" + id +
               ", name='" + name + '\'' +
               ", externalId='" + externalId + '\'' +
               ", dateCreated=" + dateCreated +
               ", activated=" + activated +
               '}';
    }

    public Boolean getActivated() {
        return activated;
    }

    public void setActivated(Boolean activated) {
        this.activated = activated;
    }

    public Instant getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Instant dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
