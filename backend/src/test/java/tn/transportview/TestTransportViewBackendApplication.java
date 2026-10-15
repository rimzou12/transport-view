package tn.transportview;

import org.springframework.boot.SpringApplication;

public class TestTransportViewBackendApplication {

	public static void main(String[] args) {
		SpringApplication.from(TransportViewBackendApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
