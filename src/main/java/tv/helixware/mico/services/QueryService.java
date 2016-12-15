package tv.helixware.mico.services;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Query the remote MICO server using SPARQL.
 *
 * @since 0.2.0
 */
@Service
@Slf4j
public class QueryService {

    /**
     * The Http content type.
     *
     * @since 0.2.0
     */
    private final static ContentType CONTENT_TYPE = ContentType.create("application/sparql-query", "UTF-8");

    /**
     * The Http accept header.
     *
     * @since 0.2.0
     */
    private final static Header ACCEPT = new BasicHeader("Accept", "application/sparql-results+json");

    /**
     * The SPARQL SELECT URL, where SELECT queries need to be posted.
     *
     * @since 0.2.0
     */
    @Value("${mico.sparql.select-url}")
    private String url;

    /**
     * Query the remote SPARQL endpoint and return the response.
     *
     * @param statement The SPARQL query.
     * @return The response content or null in case of errors.
     * @since 0.2.0
     */
    public String query(final String statement) {

        // Request:
        // * method: POST
        // * URL: `url`
        // * headers:
        //  * Accept: application/sparql-results+json
        //  * Content-Type: application/sparql-query;charset=UTF-8

        // Open the Http client.
        try (val client = HttpClients.createDefault()) {

            // Create the POST request.
            val post = new HttpPost(this.url);
            post.setEntity(new StringEntity(statement, CONTENT_TYPE));
            post.setHeader(ACCEPT);

            // Execute the request.
            try (val response = client.execute(post)) {

                // Return the response as String.
                return IOUtils.toString(response.getEntity().getContent());
            }

        } catch (IOException e) {
            log.error("An error occurred while querying the remote SPARQL endpoint.", e);
        }

        // An error occurred return null.
        return null;
    }

}
