package tn.transportview.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalTime;

@Entity
@Table(name = "stop_times")
public class StopTimeEntity {

    @EmbeddedId
    private StopTimeId id;

    @Column(name = "stop_id", nullable = false)
    private String stopId;

    @Column(name = "arrival", nullable = false)
    private LocalTime arrival;

    @Column(name = "departure", nullable = false)
    private LocalTime departure;

    protected StopTimeEntity() {
    }

    public StopTimeEntity(String tripId, String stopId, int stopSequence, LocalTime arrival, LocalTime departure) {
        this.id = new StopTimeId(tripId, stopSequence);
        this.stopId = stopId;
        this.arrival = arrival;
        this.departure = departure;
    }

    public String getTripId() {
        return id.getTripId();
    }

    public String getStopId() {
        return stopId;
    }

    public int getStopSequence() {
        return id.getStopSequence();
    }

    public LocalTime getArrival() {
        return arrival;
    }

    public LocalTime getDeparture() {
        return departure;
    }
}
