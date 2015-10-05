package tv.helixware.mico.persist;

import org.springframework.data.repository.PagingAndSortingRepository;
import tv.helixware.mico.model.ContentItem;

/**
 * Handles {@link ContentItem}s persistence and REST.
 *
 * @since 1.0.0
 */
public interface ContentItemRepository extends PagingAndSortingRepository<ContentItem, Long> {
}
