package tn.transportview.domain.port;

import java.util.List;
import tn.transportview.domain.model.Route;
import tn.transportview.domain.model.Stop;
import tn.transportview.domain.model.StopTime;
import tn.transportview.domain.model.Trip;

public interface TransitDataWriterPort {

    void saveStops(List<Stop> stops);

    void saveRoutes(List<Route> routes);

    void saveTrips(List<Trip> trips);

    void saveStopTimes(List<StopTime> stopTimes);
}
