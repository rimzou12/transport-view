package tn.transportview.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import tn.transportview.domain.model.TransportMode;

@Entity
@Table(name = "routes")
public class RouteEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(name = "short_name", nullable = false)
    private String shortName;

    @Column(name = "long_name", nullable = false)
    private String longName;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false)
    private TransportMode mode;

    protected RouteEntity() {
    }

    public RouteEntity(String id, String shortName, String longName, TransportMode mode) {
        this.id = id;
        this.shortName = shortName;
        this.longName = longName;
        this.mode = mode;
    }

    public String getId() {
        return id;
    }

    public String getShortName() {
        return shortName;
    }

    public String getLongName() {
        return longName;
    }

    public TransportMode getMode() {
        return mode;
    }
}
