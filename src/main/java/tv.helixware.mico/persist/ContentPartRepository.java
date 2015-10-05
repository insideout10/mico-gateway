package tv.helixware.mico.persist;

import org.springframework.data.repository.PagingAndSortingRepository;
import tv.helixware.mico.model.ContentPart;

/**
 * Provides persistence and REST (via Spring Data REST) access to {@link ContentPart}s.
 *
 * @since 1.0.0
 */
public interface ContentPartRepository extends PagingAndSortingRepository<ContentPart, Long> {
}
