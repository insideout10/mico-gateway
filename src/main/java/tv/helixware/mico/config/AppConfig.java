package tv.helixware.mico.config;

import com.github.anno4j.Anno4j;
import lombok.val;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.sparql.SPARQLRepository;
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
 * @since 0.1.0
 */
@Configuration
@EnableJpaAuditing
public class AppConfig {

    @Value("${mico.server:demo}")
    private String server;

    @Value("${mico.path:broker/}")
    private String path;

    @Value("${mico.username}")
    private String username;

    @Value("${mico.password}")
    private String password;

    /**
     * Make the application events asynchronous.
     *
     * @param maxThreads
     * @return
     * @since 0.1.0
     */
    @Bean
    public ApplicationEventMulticaster applicationEventMulticaster(@Value("${spring.events.max-threads:10}") final Integer maxThreads) {

        final SimpleApplicationEventMulticaster applicationEventMulticaster = new SimpleApplicationEventMulticaster();
        applicationEventMulticaster.setTaskExecutor(Executors.newFixedThreadPool(maxThreads));
        return applicationEventMulticaster;

    }

    /**
     * Provide an instance of {@link Anno4j}.
     *
     * @return An {@link Anno4j} instance.
     * @throws RepositoryConfigException
     * @throws RepositoryException
     * @since 0.2.0
     */
    @Bean
    public Anno4j anno4j() throws RepositoryConfigException, RepositoryException {

        return new Anno4j(repository());
    }

    /**
     * Get a {@link Repository} for {@link Anno4j}.
     *
     * @return A {@link Repository} instance set using the server/username/password configuration.
     * @throws RepositoryException
     * @since 0.2.0
     */
    @Bean
    public Repository repository() throws RepositoryException {

        // Configuring the repository for Anno4j, but using the default Anno4j IDGenerator
        val repository = new SPARQLRepository("http://" + server + "/marmotta/sparql/select", "http://" + server + "/marmotta/sparql/update");
        repository.setUsernameAndPassword(username, password);
        repository.initialize();

        return repository;
    }

}
