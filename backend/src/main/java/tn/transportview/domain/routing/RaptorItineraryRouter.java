package tn.transportview.domain.routing;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import tn.transportview.domain.model.Itinerary;
import tn.transportview.domain.model.Leg;
import tn.transportview.domain.model.Route;
import tn.transportview.domain.model.Stop;
import tn.transportview.domain.model.StopTime;
import tn.transportview.domain.port.StopRepositoryPort;
import tn.transportview.domain.port.TransitScheduleRepositoryPort;

/**
 * Trip-based RAPTOR-inspired itinerary router: a round-based scan where each round represents
 * one additional transfer, producing a Pareto frontier over (arrival time, transfer count).
 *
 * <p>This scans trips directly rather than grouping them into route patterns the way canonical
 * RAPTOR does — a deliberate simplification appropriate while the imported network has only a
 * handful of trips per route (see issue #2). Grouping by route pattern would be the natural next
 * optimization if trip volume grows enough for per-stop trip scans to get expensive.
 *
 * <p>Falls back to a straight-line taxi estimate ({@link TaxiLegEstimator}) when no stop is
 * reachable near the origin/destination, or when no connecting trips exist between them.
 */
public class RaptorItineraryRouter implements ItineraryRouter {

    private static final double ACCESS_RADIUS_METERS = 1_000;
    private static final int MAX_ROUNDS = 4;
    private static final int MAX_RESULTS = 5;

    private final StopRepositoryPort stopRepositoryPort;
    private final TransitScheduleRepositoryPort scheduleRepositoryPort;

    public RaptorItineraryRouter(StopRepositoryPort stopRepositoryPort, TransitScheduleRepositoryPort scheduleRepositoryPort) {
        this.stopRepositoryPort = stopRepositoryPort;
        this.scheduleRepositoryPort = scheduleRepositoryPort;
    }

    @Override
    public List<Itinerary> search(JourneyRequest request) {
        List<Stop> originStops = stopRepositoryPort.findNearby(request.origin(), ACCESS_RADIUS_METERS);
        List<Stop> destinationStops = stopRepositoryPort.findNearby(request.destination(), ACCESS_RADIUS_METERS);

        if (originStops.isEmpty() || destinationStops.isEmpty()) {
            return taxiFallback(request);
        }

        List<Map<String, RaptorLabel>> rounds = runRounds(originStops, request.departureTime());
        Set<String> destinationIds = destinationStops.stream().map(Stop::id).collect(Collectors.toSet());

        List<Itinerary> itineraries = collectParetoItineraries(rounds, destinationIds);
        return itineraries.isEmpty() ? taxiFallback(request) : itineraries;
    }

    private List<Itinerary> taxiFallback(JourneyRequest request) {
        return List.of(TaxiLegEstimator.estimateItinerary(request.origin(), request.destination(), request.departureTime()));
    }

    private List<Map<String, RaptorLabel>> runRounds(List<Stop> originStops, LocalTime departureTime) {
        List<Map<String, RaptorLabel>> rounds = new ArrayList<>();
        Map<String, RaptorLabel> round0 = new HashMap<>();
        for (Stop stop : originStops) {
            round0.put(stop.id(), new RaptorLabel(departureTime, null, null));
        }
        rounds.add(round0);

        for (int k = 1; k <= MAX_ROUNDS; k++) {
            Map<String, RaptorLabel> previous = rounds.get(k - 1);
            Map<String, RaptorLabel> current = new HashMap<>(previous);
            boolean improved = false;

            for (Map.Entry<String, RaptorLabel> entry : previous.entrySet()) {
                improved |= scanTripsFromStop(entry.getKey(), entry.getValue().arrivalTime(), current);
            }

            rounds.add(current);
            if (!improved) {
                break;
            }
        }
        return rounds;
    }

    private boolean scanTripsFromStop(String boardingStopId, LocalTime notBefore, Map<String, RaptorLabel> current) {
        boolean improved = false;
        for (StopTime boarding : scheduleRepositoryPort.findStopTimesForStop(boardingStopId)) {
            if (boarding.departure().isBefore(notBefore)) {
                continue;
            }
            List<StopTime> tripStopTimes = scheduleRepositoryPort.findStopTimesForTrip(boarding.tripId());
            int boardingIndex = indexOfStop(tripStopTimes, boardingStopId, boarding.stopSequence());
            if (boardingIndex < 0) {
                continue;
            }

            Route route = scheduleRepositoryPort.findRouteForTrip(boarding.tripId());
            improved |= rideTripFrom(tripStopTimes, boardingIndex, boardingStopId, boarding.departure(), route, current);
        }
        return improved;
    }

    private boolean rideTripFrom(
            List<StopTime> tripStopTimes,
            int boardingIndex,
            String boardingStopId,
            LocalTime boardingDeparture,
            Route route,
            Map<String, RaptorLabel> current) {
        boolean improved = false;
        for (int i = boardingIndex + 1; i < tripStopTimes.size(); i++) {
            StopTime alighting = tripStopTimes.get(i);
            RaptorLabel existing = current.get(alighting.stopId());
            if (existing != null && !alighting.arrival().isAfter(existing.arrivalTime())) {
                continue;
            }
            Leg leg = buildLeg(route, boardingStopId, boardingDeparture, alighting);
            current.put(alighting.stopId(), new RaptorLabel(alighting.arrival(), leg, boardingStopId));
            improved = true;
        }
        return improved;
    }

    private Leg buildLeg(Route route, String boardingStopId, LocalTime boardingDeparture, StopTime alighting) {
        Stop fromStop = stopRepositoryPort.findById(boardingStopId)
                .orElseThrow(() -> new IllegalStateException("Stop not found: " + boardingStopId));
        Stop toStop = stopRepositoryPort.findById(alighting.stopId())
                .orElseThrow(() -> new IllegalStateException("Stop not found: " + alighting.stopId()));
        return new Leg(route.mode(), fromStop, toStop, boardingDeparture, alighting.arrival(), route.shortName());
    }

    private static int indexOfStop(List<StopTime> tripStopTimes, String stopId, int stopSequence) {
        for (int i = 0; i < tripStopTimes.size(); i++) {
            StopTime st = tripStopTimes.get(i);
            if (st.stopId().equals(stopId) && st.stopSequence() == stopSequence) {
                return i;
            }
        }
        return -1;
    }

    private List<Itinerary> collectParetoItineraries(List<Map<String, RaptorLabel>> rounds, Set<String> destinationIds) {
        List<Itinerary> candidates = new ArrayList<>();
        for (int k = 1; k < rounds.size(); k++) {
            Map<String, RaptorLabel> labels = rounds.get(k);
            Map<String, RaptorLabel> previous = rounds.get(k - 1);
            for (String destinationId : destinationIds) {
                RaptorLabel label = labels.get(destinationId);
                if (label == null || label.viaLeg() == null || label == previous.get(destinationId)) {
                    continue;
                }
                candidates.add(reconstructItinerary(rounds, k, destinationId));
            }
        }

        candidates.sort(Comparator
                .comparing((Itinerary it) -> it.legs().get(it.legs().size() - 1).arrival())
                .thenComparing(Itinerary::transferCount));
        return candidates.stream().limit(MAX_RESULTS).toList();
    }

    private Itinerary reconstructItinerary(List<Map<String, RaptorLabel>> rounds, int round, String destinationId) {
        List<Leg> legs = new ArrayList<>();
        int currentRound = round;
        String currentStopId = destinationId;
        while (currentRound >= 1) {
            RaptorLabel label = rounds.get(currentRound).get(currentStopId);
            if (label == null || label.viaLeg() == null) {
                break;
            }
            legs.add(0, label.viaLeg());
            currentStopId = label.previousStop();
            currentRound--;
        }
        return new Itinerary(legs);
    }

    private record RaptorLabel(LocalTime arrivalTime, Leg viaLeg, String previousStop) {
    }
}
