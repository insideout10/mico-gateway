package tv.helixware.mico;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import tv.helixware.mico.services.AssetService;
import tv.helixware.mico.services.ContentItemService;
import tv.helixware.mico.services.ContentPartService;

import java.io.File;

/**
 * @since 1.0.0
 */
@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = MicoGatewayApplication.class)
@WebAppConfiguration
public class MicoClientTest {

    private final static String MIME_TYPE = "video/mp4";

    @Autowired
    private AssetService assetService;

    @Autowired
    private ContentItemService contentItemService;

    @Autowired
    private ContentPartService contentPartService;

    @Test
    public void testWildAnimals() throws Exception {

        // Create a content item, if successful create a content part with the file and then process the annotations.
        contentItemService
                .create(AssetService.NULL_ASSET).ifPresent(ci -> contentPartService
                .create(ci, MIME_TYPE, RandomStringUtils.randomAlphanumeric(12) + ".mp4", new File(getClass().getClassLoader().getResource("wild_animals.mp4").getFile()))
                .ifPresent(contentPartService::process));

    }

    @Test
    public void testMohamed() throws Exception {

        // Create a content item, if successful create a content part with the file and then process the annotations.
        contentItemService
                .create(AssetService.NULL_ASSET).ifPresent(ci -> contentPartService
                .create(ci, MIME_TYPE, RandomStringUtils.randomAlphanumeric(12) + ".mp4", new File(getClass().getClassLoader().getResource("mohamed.mp4").getFile()))
                .ifPresent(contentPartService::process));

    }

}