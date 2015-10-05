package tv.helixware.mico;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
public class MicoGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(MicoGatewayApplication.class, args);
    }
}
