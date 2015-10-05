package tv.helixware.mico.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tv.helixware.mico.model.Asset;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @since 1.0.0
 */
@Slf4j
@Service
public class AssetService {

    private final static String MIME_TYPE = "video/mp4";

    private final ContentItemService contentItemService;
    private final ContentPartService contentPartService;

    public final static Asset NULL_ASSET = new Asset();

    /**
     * Create an instance of the AssetService.
     *
     * @param contentItemService
     * @param contentPartService
     * @since 1.0.0
     */
    @Autowired
    public AssetService(final ContentItemService contentItemService, final ContentPartService contentPartService) {

        this.contentItemService = contentItemService;
        this.contentPartService = contentPartService;
    }

    /**
     * Have MICO process an {@link Asset}.
     *
     * @param asset
     * @since 1.0.0
     */
    public void upload(final Asset asset) {

        // Get the URL for the asset.
        final URL url;
        try {
            url = new URL(asset.getUrl());
        } catch (MalformedURLException e) {
            log.error(String.format("The URL is invalid [ url :: %s ]", asset.getUrl()), e);
            return;
        }

        // Create a content item, if successful create a content part with the file and then process the annotations.
        contentItemService.create(asset).ifPresent(ci -> contentPartService
                .create(ci, MIME_TYPE, RandomStringUtils.randomAlphanumeric(12) + ".mp4", url)
                .ifPresent(cp -> {
                    contentItemService.submit(ci); // Submit the content item.
                    contentPartService.process(cp); // Process the results for the content part.
                }));

    }

}
