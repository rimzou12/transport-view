package tn.transportview.application.usecase;

import java.io.InputStream;
import tn.transportview.domain.model.GtfsFeedData;
import tn.transportview.domain.model.ImportSummary;
import tn.transportview.domain.port.GtfsFeedParserPort;
import tn.transportview.domain.port.TransitDataWriterPort;

public class ImportGtfsFeedUseCase {

    private final GtfsFeedParserPort gtfsFeedParserPort;
    private final TransitDataWriterPort transitDataWriterPort;

    public ImportGtfsFeedUseCase(GtfsFeedParserPort gtfsFeedParserPort, TransitDataWriterPort transitDataWriterPort) {
        this.gtfsFeedParserPort = gtfsFeedParserPort;
        this.transitDataWriterPort = transitDataWriterPort;
    }

    public ImportSummary execute(InputStream gtfsZip) {
        GtfsFeedData feed = gtfsFeedParserPort.parse(gtfsZip);

        transitDataWriterPort.saveStops(feed.stops());
        transitDataWriterPort.saveRoutes(feed.routes());
        transitDataWriterPort.saveTrips(feed.trips());
        transitDataWriterPort.saveStopTimes(feed.stopTimes());

        return new ImportSummary(feed.stops().size(), feed.routes().size(), feed.trips().size(), feed.stopTimes().size());
    }
}
