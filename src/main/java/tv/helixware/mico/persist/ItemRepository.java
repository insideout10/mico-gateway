package tv.helixware.mico.persist;

import org.springframework.data.repository.PagingAndSortingRepository;
import tv.helixware.mico.model.Item;

/**
 * Handles {@link Item}s persistence and REST.
 *
 * @since 1.0.0
 */
public interface ItemRepository extends PagingAndSortingRepository<Item, Long> {
}
