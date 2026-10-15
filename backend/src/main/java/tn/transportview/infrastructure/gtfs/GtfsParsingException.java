package tn.transportview.infrastructure.gtfs;

/**
 * Thrown for a malformed or unreadable GTFS feed. Extends {@link IllegalArgumentException}
 * so the global exception handler reports it as a 400 (bad input), not a 500.
 */
public class GtfsParsingException extends IllegalArgumentException {

    public GtfsParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public GtfsParsingException(String message) {
        super(message);
    }
}
