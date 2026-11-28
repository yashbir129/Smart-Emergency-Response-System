package com.emergency.system.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "emergency_events")
public class EmergencyEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long emergencyId;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private Long actorId;

    @Column(nullable = false)
    private String actorRole;

    @Column(nullable = false, length = 2000)
    private String message;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected EmergencyEvent() {
    }

    public EmergencyEvent(Long emergencyId, String status, Long actorId, String actorRole, String message) {
        this.emergencyId = emergencyId;
        this.status = status;
        this.actorId = actorId;
        this.actorRole = actorRole;
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public Long getEmergencyId() {
        return emergencyId;
    }

    public String getStatus() {
        return status;
    }

    public Long getActorId() {
        return actorId;
    }

    public String getActorRole() {
        return actorRole;
    }

    public String getMessage() {
        return message;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
