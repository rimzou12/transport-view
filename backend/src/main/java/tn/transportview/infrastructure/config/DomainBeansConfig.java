package tn.transportview.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tn.transportview.application.usecase.ImportGtfsFeedUseCase;
import tn.transportview.application.usecase.SearchItinerariesUseCase;
import tn.transportview.application.usecase.SearchStopsUseCase;
import tn.transportview.domain.port.GtfsFeedParserPort;
import tn.transportview.domain.port.StopRepositoryPort;
import tn.transportview.domain.port.TransitDataWriterPort;
import tn.transportview.domain.port.TransitScheduleRepositoryPort;
import tn.transportview.domain.routing.ItineraryRouter;
import tn.transportview.domain.routing.RaptorItineraryRouter;

@Configuration
public class DomainBeansConfig {

    @Bean
    public ItineraryRouter itineraryRouter(StopRepositoryPort stopRepositoryPort, TransitScheduleRepositoryPort scheduleRepositoryPort) {
        return new RaptorItineraryRouter(stopRepositoryPort, scheduleRepositoryPort);
    }

    @Bean
    public SearchItinerariesUseCase searchItinerariesUseCase(ItineraryRouter itineraryRouter) {
        return new SearchItinerariesUseCase(itineraryRouter);
    }

    @Bean
    public ImportGtfsFeedUseCase importGtfsFeedUseCase(GtfsFeedParserPort gtfsFeedParserPort, TransitDataWriterPort transitDataWriterPort) {
        return new ImportGtfsFeedUseCase(gtfsFeedParserPort, transitDataWriterPort);
    }

    @Bean
    public SearchStopsUseCase searchStopsUseCase(StopRepositoryPort stopRepositoryPort) {
        return new SearchStopsUseCase(stopRepositoryPort);
    }
}
