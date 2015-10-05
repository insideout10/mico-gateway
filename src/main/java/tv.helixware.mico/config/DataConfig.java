package tv.helixware.mico.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enable JPA auditing for Entity annotations such as createdAt.
 *
 * @since 1.0.0
 */
@Configuration
@EnableJpaAuditing
public class DataConfig {

}
