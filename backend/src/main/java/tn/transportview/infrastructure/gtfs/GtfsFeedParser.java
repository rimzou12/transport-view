package tn.transportview.infrastructure.gtfs;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tn.transportview.domain.model.Coordinates;
import tn.transportview.domain.model.GtfsFeedData;
import tn.transportview.domain.model.Route;
import tn.transportview.domain.model.Stop;
import tn.transportview.domain.model.StopTime;
import tn.transportview.domain.model.TransportMode;
import tn.transportview.domain.model.Trip;
import tn.transportview.domain.port.GtfsFeedParserPort;

/**
 * Parses a GTFS feed ZIP into domain model lists, keeping only the route types we support
 * (see {@link GtfsRouteTypeMapper}) and interpolating blank stop_times (see
 * {@link GtfsStopTimeInterpolator}). Real-world TRANSTU exports have been observed with a
 * UTF-8 BOM, inconsistent column order, and stray whitespace/tab characters inside id fields —
 * all fields are read by header name and trimmed defensively.
 */
@Component
public class GtfsFeedParser implements GtfsFeedParserPort {

    private static final Logger log = LoggerFactory.getLogger(GtfsFeedParser.class);

    private static final CSVFormat CSV_FORMAT = CSVFormat.DEFAULT.builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .setIgnoreSurroundingSpaces(true)
            .build();

    @Override
    public GtfsFeedData parse(InputStream gtfsZip) {
        Map<String, byte[]> tables = readZipTables(gtfsZip);

        List<Route> routes = parseRoutes(requireTable(tables, "routes.txt"));
        Set<String> supportedRouteIds = routes.stream().map(Route::id).collect(Collectors.toSet());

        List<Trip> trips = parseTrips(requireTable(tables, "trips.txt"), supportedRouteIds);
        Set<String> supportedTripIds = trips.stream().map(Trip::id).collect(Collectors.toSet());

        List<Stop> stops = parseStops(requireTable(tables, "stops.txt"));
        List<StopTime> stopTimes = parseStopTimes(requireTable(tables, "stop_times.txt"), supportedTripIds);

        return new GtfsFeedData(stops, routes, trips, stopTimes);
    }

    private static Map<String, byte[]> readZipTables(InputStream gtfsZip) {
        Map<String, byte[]> tables = new HashMap<>();
        try (ZipInputStream zis = new ZipInputStream(gtfsZip)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    tables.put(entry.getName(), zis.readAllBytes());
                }
            }
        } catch (IOException e) {
            throw new GtfsParsingException("Failed to read GTFS zip", e);
        }
        return tables;
    }

    private static byte[] requireTable(Map<String, byte[]> tables, String fileName) {
        byte[] bytes = tables.get(fileName);
        if (bytes == null) {
            throw new GtfsParsingException("GTFS feed is missing required file: " + fileName);
        }
        return bytes;
    }

    private static List<Route> parseRoutes(byte[] bytes) {
        List<Route> routes = new ArrayList<>();
        for (CSVRecord record : parseCsv(bytes)) {
            Optional<TransportMode> mode = GtfsRouteTypeMapper.toTransportMode(clean(record.get("route_type")));
            if (mode.isEmpty()) {
                continue;
            }
            routes.add(new Route(
                    clean(record.get("route_id")),
                    clean(record.get("route_short_name")),
                    clean(record.get("route_long_name")),
                    mode.get()));
        }
        return routes;
    }

    private static List<Trip> parseTrips(byte[] bytes, Set<String> supportedRouteIds) {
        List<Trip> trips = new ArrayList<>();
        for (CSVRecord record : parseCsv(bytes)) {
            String routeId = clean(record.get("route_id"));
            if (!supportedRouteIds.contains(routeId)) {
                continue;
            }
            trips.add(new Trip(clean(record.get("trip_id")), routeId, clean(record.get("trip_headsign"))));
        }
        return trips;
    }

    private static List<Stop> parseStops(byte[] bytes) {
        List<Stop> stops = new ArrayList<>();
        for (CSVRecord record : parseCsv(bytes)) {
            try {
                double lat = Double.parseDouble(clean(record.get("stop_lat")));
                double lon = Double.parseDouble(clean(record.get("stop_lon")));
                stops.add(new Stop(clean(record.get("stop_id")), clean(record.get("stop_name")), new Coordinates(lat, lon)));
            } catch (IllegalArgumentException ex) {
                log.warn("Skipping malformed stop row (stop_id={}): {}", record.get("stop_id"), ex.getMessage());
            }
        }
        return stops;
    }

    private static List<StopTime> parseStopTimes(byte[] bytes, Set<String> supportedTripIds) {
        Map<String, List<GtfsStopTimeRow>> rowsByTrip = new LinkedHashMap<>();
        for (CSVRecord record : parseCsv(bytes)) {
            String tripId = clean(record.get("trip_id"));
            if (!supportedTripIds.contains(tripId)) {
                continue;
            }
            GtfsStopTimeRow row = new GtfsStopTimeRow(
                    tripId,
                    clean(record.get("stop_id")),
                    Integer.parseInt(clean(record.get("stop_sequence"))),
                    parseGtfsTime(record.get("arrival_time")),
                    parseGtfsTime(record.get("departure_time")));
            rowsByTrip.computeIfAbsent(tripId, key -> new ArrayList<>()).add(row);
        }

        List<StopTime> stopTimes = new ArrayList<>();
        for (List<GtfsStopTimeRow> rows : rowsByTrip.values()) {
            rows.sort(Comparator.comparingInt(GtfsStopTimeRow::stopSequence));
            stopTimes.addAll(GtfsStopTimeInterpolator.interpolate(rows));
        }
        return stopTimes;
    }

    private static LocalTime parseGtfsTime(String raw) {
        String value = clean(raw);
        if (value.isEmpty()) {
            return null;
        }
        String[] parts = value.split(":");
        int hour = Integer.parseInt(parts[0]) % 24;
        int minute = Integer.parseInt(parts[1]);
        int second = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
        return LocalTime.of(hour, minute, second);
    }

    private static Iterable<CSVRecord> parseCsv(byte[] bytes) {
        try {
            return new CSVParser(new StringReader(decodeUtf8(bytes)), CSV_FORMAT);
        } catch (IOException e) {
            throw new GtfsParsingException("Failed to parse GTFS CSV table", e);
        }
    }

    private static String decodeUtf8(byte[] bytes) {
        String text = new String(bytes, StandardCharsets.UTF_8);
        return text.startsWith("﻿") ? text.substring(1) : text;
    }

    private static String clean(String value) {
        return value == null ? "" : value.replace("\t", "").trim();
    }
}
