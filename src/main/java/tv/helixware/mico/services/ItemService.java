package tv.helixware.mico.services;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tv.helixware.mico.model.Asset;
import tv.helixware.mico.model.Item;
import tv.helixware.mico.persist.ItemRepository;

import java.util.Optional;

/**
 * @since 0.1.0
 */
@Slf4j
@Service
public class ItemService {

    private final MicoClient client;

    private final ItemRepository itemRepository;

    /**
     * Create an instance of the ContentItemService.
     *
     * @param client
     * @since 0.1.0
     */
    @Autowired
    public ItemService(final MicoClient client, final ItemRepository itemRepository) {

        this.client = client;

        this.itemRepository = itemRepository;

    }

    /**
     * Crete a {@link Item} using the {@link MicoClient}.
     *
     * @return
     * @since 0.1.0
     */
    public Optional<Item> create(final Asset asset) {

        // Return the ContentItem persisted to the database.

        val item = client.create(asset).map(itemRepository::save);

        // Add some trace logging, just in case.
        if (log.isDebugEnabled()) {
            if (item.isPresent())
                log.debug(String.format("Item created [ uri :: %s ]", item.get().getUri()));
            else
                log.debug("Item not created");
        }

        return item;

    }

    /**
     * Submit a {@link Item} for processing.
     *
     * @param item
     * @return
     * @since 0.1.0
     */
    public boolean submit(final Item item) {

        // 3. Submit the Content Item.
        return client.submit(item);

    }

}