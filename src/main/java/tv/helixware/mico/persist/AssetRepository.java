package tv.helixware.mico.persist;

import org.springframework.data.repository.PagingAndSortingRepository;
import tv.helixware.mico.model.Asset;

/**
 * Provides persistence and REST (via Spring Data REST) access to {@link Asset}s.
 *
 * @since 1.0.0
 */
public interface AssetRepository extends PagingAndSortingRepository<Asset, Long> {

}
