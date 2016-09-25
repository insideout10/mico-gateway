package tv.helixware.mico.events;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;
import tv.helixware.mico.model.Asset;
import tv.helixware.mico.services.AssetService;

/**
 * Handles events related to {@link Asset}s.
 *
 * @since 0.1.0
 */
@Slf4j
@Component
@RepositoryEventHandler(Asset.class)
public class AssetEventHandler {

    private final AssetService assetService;

    @Autowired
    public AssetEventHandler(final AssetService assetService) {

        this.assetService = assetService;

    }

    @HandleAfterCreate
    public void handleAssetAfterCreate(Asset asset) {

        log.info(String.format("An asset has been created [ id :: %d ]", asset.getId()));
        assetService.upload(asset);
        
    }

}
