package tv.helixware.mico.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tv.helixware.mico.model.Asset;
import tv.helixware.mico.model.ContentItem;
import tv.helixware.mico.persist.ContentItemRepository;

import java.util.Optional;

/**
 * @since 1.0.0
 */
@Slf4j
@Service
public class ContentItemService {

    private final MicoClient client;

    private final ContentItemRepository contentItemRepository;

    /**
     * Create an instance of the ContentItemService.
     *
     * @param client
     * @since 1.0.0
     */
    @Autowired
    public ContentItemService(final MicoClient client, final ContentItemRepository contentItemRepository) {

        this.client = client;

        this.contentItemRepository = contentItemRepository;

    }

    /**
     * Crete a {@link ContentItem} using the {@link MicoClient}.
     *
     * @return
     * @since 1.0.0
     */
    public Optional<ContentItem> create(final Asset asset) {

        // Return the ContentItem persisted to the database.
        return client.create(asset).map(contentItemRepository::save);

    }

    /**
     * Submit a {@link ContentItem} for processing.
     *
     * @param contentItem
     * @return
     * @since 1.0.0
     */
    public boolean submit(final ContentItem contentItem) {

        // 3. Submit the Content Item.
        return client.submit(contentItem);

    }

}