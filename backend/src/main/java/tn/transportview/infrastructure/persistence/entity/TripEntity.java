package tn.transportview.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "trips")
public class TripEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(name = "route_id", nullable = false)
    private String routeId;

    @Column(name = "headsign")
    private String headsign;

    protected TripEntity() {
    }

    public TripEntity(String id, String routeId, String headsign) {
        this.id = id;
        this.routeId = routeId;
        this.headsign = headsign;
    }

    public String getId() {
        return id;
    }

    public String getRouteId() {
        return routeId;
    }

    public String getHeadsign() {
        return headsign;
    }
}
