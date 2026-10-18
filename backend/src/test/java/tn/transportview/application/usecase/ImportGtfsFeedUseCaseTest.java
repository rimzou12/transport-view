package tn.transportview.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.transportview.domain.model.Coordinates;
import tn.transportview.domain.model.GtfsFeedData;
import tn.transportview.domain.model.ImportSummary;
import tn.transportview.domain.model.Route;
import tn.transportview.domain.model.Stop;
import tn.transportview.domain.model.StopTime;
import tn.transportview.domain.model.TransportMode;
import tn.transportview.domain.model.Trip;
import tn.transportview.domain.port.GtfsFeedParserPort;
import tn.transportview.domain.port.TransitDataWriterPort;

@ExtendWith(MockitoExtension.class)
class ImportGtfsFeedUseCaseTest {

    @Mock
    private GtfsFeedParserPort gtfsFeedParserPort;

    @Mock
    private TransitDataWriterPort transitDataWriterPort;

    @Test
    void parsesFeedAndPersistsEachEntityTypeInDependencyOrder() {
        Stop stop = new Stop("STOP_1", "Tunis Marine", new Coordinates(36.8008, 10.1815));
        Route route = new Route("ligne1", "ligne1_metro", "Tunis Marine to Ben Arous", TransportMode.METRO);
        Trip trip = new Trip("ligne1010", "ligne1", "Tunis Marine");
        StopTime stopTime = new StopTime("ligne1010", "STOP_1", 1, LocalTime.of(23, 33), LocalTime.of(23, 33));

        GtfsFeedData feedData = new GtfsFeedData(List.of(stop), List.of(route), List.of(trip), List.of(stopTime));
        InputStream gtfsZip = new ByteArrayInputStream(new byte[0]);
        when(gtfsFeedParserPort.parse(gtfsZip)).thenReturn(feedData);

        ImportGtfsFeedUseCase useCase = new ImportGtfsFeedUseCase(gtfsFeedParserPort, transitDataWriterPort);
        ImportSummary summary = useCase.execute(gtfsZip);

        verify(transitDataWriterPort).saveStops(List.of(stop));
        verify(transitDataWriterPort).saveRoutes(List.of(route));
        verify(transitDataWriterPort).saveTrips(List.of(trip));
        verify(transitDataWriterPort).saveStopTimes(List.of(stopTime));
        assertThat(summary).isEqualTo(new ImportSummary(1, 1, 1, 1));
    }
}
