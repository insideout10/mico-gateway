package tv.helixware.mico;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.RandomStringUtils;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import tv.helixware.mico.model.Asset;
import tv.helixware.mico.model.Item;
import tv.helixware.mico.model.Part;
import tv.helixware.mico.services.ItemService;
import tv.helixware.mico.services.MicoClient;
import tv.helixware.mico.services.PartService;

import java.io.File;
import java.util.UUID;

import static org.junit.Assert.assertTrue;

/**
 * @since 1.0.0
 */
@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MicoGatewayApplication.class)
@WebAppConfiguration
public class MicoClientTest {

    private final static String MICO_TYPE = "mico:Video";

    @Autowired
    private MicoClient client;

    @Autowired
    private ItemService itemService;

    @Autowired
    private PartService partService;

    /**
     * Test creating a MICO {@link Item} and a {@link Part}.
     *
     * @since 0.2.0
     */
    @Test
    public void test() {

        // Generate a fake asset.
        val asset = asset();

        // Call MICO to create an item.
        val optItem = client.create(asset);

        // Check if the item has been created.
        assertTrue(optItem.isPresent());

        // Get the item from the Optional.
        val item = optItem.get();

        // Generate a random filename for MICO.
        val name = RandomStringUtils.randomAlphanumeric(12) + ".mp4";

        // Add the content part to the item.
        val optPart = client.addContentPart(item, MICO_TYPE, name, wildAnimalsFile());

        // Check if the part has been added.
        assertTrue(optPart.isPresent());

        // Get the part.
        val part = optPart.get();

        // Submit the item.
        val result = client.submit(item);

        // Check that submission was positive.
        assertTrue(result);

        // Finally process the part.
        partService.process(part);

    }

    @Test
    public void testWildAnimals() throws Exception {

        // Create a content item, if successful create a content part with the file and then process the annotations.
        itemService
                .create(null).ifPresent(ci -> partService
                .create(ci, MICO_TYPE, RandomStringUtils.randomAlphanumeric(12) + ".mp4", new File(getClass().getClassLoader().getResource("wild_animals.mp4").getFile()))
                .ifPresent(partService::process));

    }

    @Test
    public void testMohamed() throws Exception {

        // Create a content item, if successful create a content part with the file and then process the annotations.
        itemService
                .create(null).ifPresent(ci -> partService
                .create(ci, MICO_TYPE, RandomStringUtils.randomAlphanumeric(12) + ".mp4", new File(getClass().getClassLoader().getResource("mohamed.mp4").getFile()))
                .ifPresent(partService::process));

    }

    /**
     * Create a mock {@link Asset}.
     *
     * @return A mock {@link Asset} instance.
     * @since 0.2.0
     */
    private Asset asset() {

        return new Asset(0L, "http://example.org/asset.mp4", UUID.randomUUID().toString(), 1L, DateTime.now(), DateTime.now());
    }

    /**
     * Get a reference to the wild_animals.mp4 test file.
     *
     * @return A {@link File} instance.
     * @since 0.2.0
     */
    private File wildAnimalsFile() {

        return new File(getClass().getClassLoader().getResource("wild_animals.mp4").getFile());
    }

}
