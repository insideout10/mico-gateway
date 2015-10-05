package tv.helixware.mico.persist;

import org.springframework.data.repository.PagingAndSortingRepository;
import tv.helixware.mico.model.Fragment;

/**
 * Provides persistency and REST access (via Spring Data REST) to Fragments.
 *
 * @since 1.0.0
 */
public interface FragmentRepository extends PagingAndSortingRepository<Fragment, Long> {
}
