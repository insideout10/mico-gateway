package tv.helixware.mico.factories;

import com.github.anno4j.Anno4j;
import com.github.anno4j.querying.QueryService;
import eu.mico.platform.anno4j.model.namespaces.MMM;
import eu.mico.platform.anno4j.model.namespaces.MMMTERMS;
import lombok.RequiredArgsConstructor;
import org.openrdf.repository.RepositoryException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Creates Anno4j {@link QueryService}s with a basic configuration.
 *
 * @since 0.2.0
 */
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
public class QueryServiceFactory {

    /**
     * The {@link Anno4j} instance.
     *
     * @since 0.2.0
     */
    private final Anno4j anno4j;

    /**
     * Create a {@link QueryService} instance configured with our basic prefixes.
     *
     * @return A {@link QueryService} instance.
     * @throws RepositoryException
     * @since 0.2.0
     */
    public QueryService create() throws RepositoryException {

        return anno4j.createQueryService()
                .addPrefix(MMM.PREFIX, MMM.NS)
                .addPrefix(MMMTERMS.PREFIX, MMMTERMS.NS);

    }

}
