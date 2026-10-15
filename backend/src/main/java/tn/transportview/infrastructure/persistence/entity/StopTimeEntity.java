package tn.transportview.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalTime;

@Entity
@Table(name = "stop_times")
public class StopTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trip_id", nullable = false)
    private String tripId;

    @Column(name = "stop_id", nullable = false)
    private String stopId;

    @Column(name = "stop_sequence", nullable = false)
    private int stopSequence;

    @Column(name = "arrival", nullable = false)
    private LocalTime arrival;

    @Column(name = "departure", nullable = false)
    private LocalTime departure;

    protected StopTimeEntity() {
    }

    public StopTimeEntity(String tripId, String stopId, int stopSequence, LocalTime arrival, LocalTime departure) {
        this.tripId = tripId;
        this.stopId = stopId;
        this.stopSequence = stopSequence;
        this.arrival = arrival;
        this.departure = departure;
    }

    public String getTripId() {
        return tripId;
    }

    public String getStopId() {
        return stopId;
    }

    public int getStopSequence() {
        return stopSequence;
    }

    public LocalTime getArrival() {
        return arrival;
    }

    public LocalTime getDeparture() {
        return departure;
    }
}
