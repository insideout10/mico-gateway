package tv.helixware.mico.persist;

import org.springframework.data.repository.PagingAndSortingRepository;
import tv.helixware.mico.model.Part;

/**
 * Provides persistence and REST (via Spring Data REST) access to {@link Part}s.
 *
 * @since 1.0.0
 */
public interface PartRepository extends PagingAndSortingRepository<Part, Long> {
}
