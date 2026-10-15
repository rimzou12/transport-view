package tn.transportview.infrastructure.persistence.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class StopTimeId implements Serializable {

    private String tripId;
    private Integer stopSequence;

    protected StopTimeId() {
    }

    public StopTimeId(String tripId, Integer stopSequence) {
        this.tripId = tripId;
        this.stopSequence = stopSequence;
    }

    public String getTripId() {
        return tripId;
    }

    public Integer getStopSequence() {
        return stopSequence;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StopTimeId other)) {
            return false;
        }
        return Objects.equals(tripId, other.tripId) && Objects.equals(stopSequence, other.stopSequence);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tripId, stopSequence);
    }
}
