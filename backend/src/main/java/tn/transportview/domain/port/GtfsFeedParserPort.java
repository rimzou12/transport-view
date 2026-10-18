package tn.transportview.domain.port;

import java.io.InputStream;
import tn.transportview.domain.model.GtfsFeedData;

public interface GtfsFeedParserPort {

    /**
     * Parses a GTFS feed (ZIP of CSV tables) into domain model lists.
     * Implementations decide which route types are supported and filter accordingly.
     */
    GtfsFeedData parse(InputStream gtfsZip);
}
