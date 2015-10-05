package tv.helixware.mico.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.concurrent.Executors;

/**
 * Enable JPA auditing for Entity annotations such as createdAt.
 *
 * @since 1.0.0
 */
@Configuration
@EnableJpaAuditing
public class AppConfig {

    /**
     * Make the application events asynchronous.
     *
     * @param maxThreads
     * @return
     * @since 1.0.0
     */
    @Bean
    public ApplicationEventMulticaster applicationEventMulticaster(@Value("${spring.events.max-threads:10}") final Integer maxThreads) {

        final SimpleApplicationEventMulticaster applicationEventMulticaster = new SimpleApplicationEventMulticaster();
        applicationEventMulticaster.setTaskExecutor(Executors.newFixedThreadPool(maxThreads));
        return applicationEventMulticaster;

    }

}
