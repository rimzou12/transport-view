package tn.transportview.domain.model;

import java.util.List;

public record GtfsFeedData(List<Stop> stops, List<Route> routes, List<Trip> trips, List<StopTime> stopTimes) {

    public GtfsFeedData {
        stops = List.copyOf(stops);
        routes = List.copyOf(routes);
        trips = List.copyOf(trips);
        stopTimes = List.copyOf(stopTimes);
    }
}
