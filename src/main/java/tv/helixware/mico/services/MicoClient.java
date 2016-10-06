package tv.helixware.mico.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tv.helixware.mico.model.Asset;
import tv.helixware.mico.model.Item;
import tv.helixware.mico.model.Part;
import tv.helixware.mico.response.CheckStatusResponse;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @since 0.1.0
 */
@Slf4j
@Component
public class MicoClient {

    private final String serverURL;

    private final ObjectMapper objectMapper;

    private final static String INJECT_CREATE_PATH = "inject/create";
    private final static String INJECT_ADD_PATH = "inject/add";
    private final static String INJECT_SUBMIT_PATH = "inject/submit";
    private final static String STATUS_ITEMS_PATH = "status/items";

    @Value("${mico.route.id:6}")
    private String routeId;

    /**
     * The property name for an item URI in MICO's responses (this has changed over time).
     *
     * @since 0.2.0
     */
    private final static String ITEM_URI = "itemUri";

    /**
     * The property name for an item URI in MICO's response when adding a content part (yes, it's different from the ITEM_URI!).
     *
     * @since 0.2.0
     */
    private final static String CONTENT_PART_ITEM_URI = "itemUri";

    /**
     * Create an instance of the IngestionService.
     *
     * @param server   The MICO server name.
     * @param username The username to access the server.
     * @param password The password to access the server.
     * @since 0.1.0
     */
    @Autowired
    public MicoClient(@Value("${mico.server}") final String server, @Value("${mico.path:broker/}") final String path, @Value("${mico.username}") final String username, @Value("${mico.password}") final String password) {

        this.serverURL = String.format("http://%s:%s@%s/%s", username, password, server, path);

        this.objectMapper = new ObjectMapper();
    }

    /**
     * Create a {@link Item}.
     *
     * @return
     * @since 0.1.0
     */
    public Optional<Item> create(final Asset asset) {

        val url = serverURL + INJECT_CREATE_PATH;

        log.debug(String.format("Creating item [ url :: %s ]", url));

        // Prepare the client and send the POST request.
        final CloseableHttpClient client = HttpClients.createDefault();
        final HttpPost post = new HttpPost(url);
        final CloseableHttpResponse response;

        // Get the response, return an empty if an error occurred.
        try {
            response = client.execute(post);
        } catch (IOException e) {
            log.error(String.format("An error occurred while querying the remote server [ url :: %s ]", url), e);
            return Optional.empty();
        }

        // If the status code is not 200 return an empty.
        if (200 != response.getStatusLine().getStatusCode()) {
            log.error(String.format("An error occurred while querying the remote server [ url :: %s ][ status line :: %s ]", url, response.getStatusLine().toString()));
            HttpClientUtils.closeQuietly(response);
            return Optional.empty();
        }

        // Get the entity.
        try {
            final String responseBody = EntityUtils.toString(response.getEntity());
            HttpClientUtils.closeQuietly(response);

            final JsonNode node = objectMapper.readTree(responseBody);

            // If the *uri* field is missing from the JSON return an empty.
            if (!node.has(ITEM_URI)) {
                log.error(String.format("The JSON is invalid [ url :: %s ][ response body :: %s ][ missing property :: %s ]", url, responseBody, ITEM_URI));
                return Optional.empty();
            }

            // Get the URI and create a new ContentItem.
            return Optional.of(createContentItem(asset, node.get(ITEM_URI).asText()));

        } catch (Exception e) {
            log.error(String.format("An error occurred while parsing the response [ url :: %s ]", url));
        }

        return Optional.empty();
    }

    /**
     * Add a {@link Part} to a {@link Item}.
     *
     * @param item
     * @param micoType
     * @param name
     * @return
     * @since 0.1.0
     */
    public Optional<Part> addContentPart(final Item item, final String micoType, final String mimeType, final String name, final File file) {

        try {
            // Build the URI and get the response.
            val url = new URIBuilder(serverURL + INJECT_ADD_PATH)
                    .setParameter(ITEM_URI, item.getUri())
                    .setParameter("type", micoType)
                    .setParameter("name", name)
                    // As of 6th Oct 2016, Marcel says not to send the mimeType parameter anymore.
                    // .setParameter("mimeType", mimeType)
                    .build();

            log.debug(String.format("Creating part [ url :: %s ]", url));

            val entity = new FileEntity(file);

            val response = post(url.toString(), Optional.of(entity));

            // If the response is empty, we return an empty.
            if (!response.isPresent())
                return Optional.empty();

            val node = objectMapper.readTree(response.get());

            // If the *uri* field is missing from the JSON return an empty.
            if (!node.has(CONTENT_PART_ITEM_URI)) {
                log.error(String.format("The JSON is invalid [ url :: %s ][ response body :: %s ]", url, response.get()));
                return Optional.empty();
            }

            // Get the URI and create a new ContentItem.
            return Optional.of(createContentPart(item, node.get(CONTENT_PART_ITEM_URI).asText(), micoType, name));

        } catch (URISyntaxException e) {
            log.error(String.format("The URL is invalid [ url :: %s ]", serverURL + INJECT_ADD_PATH), e);
        } catch (IOException e) {
            log.error(String.format("An error occurred while parsing the response [ url :: %s ]", serverURL + INJECT_ADD_PATH), e);
        }

        return Optional.empty();
    }

//    /**
//     * Add a {@link ContentPart} to a {@link ContentItem}.
//     *
//     * @param contentItem
//     * @param mimeType
//     * @param name
//     * @return
//     * @since 0.1.0
//     */
//    public Optional<ContentPart> addContentPart(final ContentItem contentItem, final String mimeType, final String name, final InputStream stream) {
//
//        try {
//            // Build the URI and get the response.
//            final URI uri = new URIBuilder(serverURL + INJECT_ADD_PATH)
//                    .setParameter("ci", contentItem.getUri())
//                    .setParameter("type", mimeType)
//                    .setParameter("name", name)
//                    .build();
//
//            final InputStreamEntity entity = new InputStreamEntity(stream, ContentType.create(mimeType));
//
//            final Optional<String> response = post(uri.toString(), Optional.of(entity));
//
//            // If the response is empty, we return an empty.
//            if (!response.isPresent())
//                return Optional.empty();
//
//            final JsonNode node = objectMapper.readTree(response.get());
//
//            // If the *uri* field is missing from the JSON return an empty.
//            if (!node.has("uri")) {
//                log.error(String.format("The JSON is invalid [ url :: %s ][ response body :: %s ]", uri, response.get()));
//                return Optional.empty();
//            }
//
//            // Get the URI and create a new ContentItem.
//            return Optional.of(createContentPart(contentItem, node.get("uri").asText(), mimeType, name));
//
//        } catch (URISyntaxException e) {
//            log.error(String.format("The URL is invalid [ url :: %s ]", serverURL + INJECT_ADD_PATH), e);
//        } catch (IOException e) {
//            log.error(String.format("An error occurred while parsing the response [ url :: %s ]", serverURL + INJECT_ADD_PATH), e);
//        }
//
//        return Optional.empty();
//    }

    /**
     * Submit the {@link Item} for processing.
     *
     * @param item
     * @since 0.1.0
     */
    public boolean submit(final Item item) {

        // Build the URI and get the response.
        try {
            val url = new URIBuilder(serverURL + INJECT_SUBMIT_PATH)
                    .setParameter("item", item.getUri())
                    .setParameter("route", routeId)
                    .build();

            log.debug(String.format("Submitting item [ url :: %s ]", url));

            val response = post(url.toString());

            if (log.isDebugEnabled()) {
                if (response.isPresent())
                    log.debug(String.format("Submit response [ %s ]", response.get()));
                else
                    log.debug("No response received while submitting an item");
            }

            // If the response is present (although empty), it's a success.
            return response.isPresent();

        } catch (URISyntaxException e) {
            log.error(String.format("An error occurred while submitting a Content Item [ url :: %s ]", serverURL + INJECT_SUBMIT_PATH));
        }

        return false;
    }

    /**
     * @param item
     * @param parts
     * @return
     * @since 0.1.0
     */
    public List<CheckStatusResponse> checkStatus(final Item item, final boolean parts) {

        log.debug(String.format("Checking status [ item uri :: %s ]", item.getUri()));

        final URI url;

        try {

            // Build the URI and get the response.
            url = new URIBuilder(serverURL + STATUS_ITEMS_PATH)
                    .setParameter("uri", item.getUri())
                    .setParameter("parts", parts ? "true" : "false")
                    .build();

        } catch (Exception e) {

            log.error("Cannot build url", e);
            return null;
        }

        try {

            log.debug(String.format("Checking status [ url :: %s ]", url));

            val response = get(url.toString());

            // If the response is empty, return empty.
            if (!response.isPresent())
                return Collections.emptyList();

            // Map the response to a CheckStatusResponse.
            return objectMapper.readValue(response.get(), new TypeReference<List<CheckStatusResponse>>() {
            });

        } catch (Exception e) {
            log.error(String.format("An error occurred while parsing a response [ url :: %s ]", url), e);
        }

        return Collections.emptyList();
    }

    // TODO: move to a ContentItemBuilder class.

    /**
     * Create a {@link Item} given a URI.
     *
     * @param uri The ContentItem URI as returned by MICO.
     * @return A ContentItem instance
     * @since 0.1.0
     */
    private Item createContentItem(final Asset asset, final String uri) {

        final String[] parts = uri.split("/");
        return new Item(asset, uri, parts[parts.length - 1]);
    }

    // TODO: move to a ContentPartBuilder class.

    /**
     * Create a {@link Part} given a URI.
     *
     * @param uri The ContentPart URI as returned by MICO.
     * @return A ContentPart instance.
     * @since 0.1.0
     */
    private Part createContentPart(final Item item, final String uri, final String mimeType, final String name) {

        final String[] parts = uri.split("/");
        return new Part(item, uri, parts[parts.length - 1], mimeType, name);
    }


    private Optional<String> request(final HttpRequestBase request) {

        // Prepare the client and send the POST request.
        final CloseableHttpClient client = HttpClients.createDefault();
        final CloseableHttpResponse response;

        // Get the response, return an empty if an error occurred.
        try {
            response = client.execute(request);
        } catch (IOException e) {
            log.error(String.format("An error occurred while querying the remote server [ url :: %s ]", request.getURI().toString()), e);
            return Optional.empty();
        }

        // If the status code is not 200 return an empty.
        if (200 != response.getStatusLine().getStatusCode()) {
            log.error(String.format("An error occurred while querying the remote server [ url :: %s ][ status line :: %s ]", request.getURI().toString(), response.getStatusLine().toString()));
            HttpClientUtils.closeQuietly(response);
            return Optional.empty();
        }

        // Get the entity.
        final String responseBody;
        try {
            responseBody = EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            log.error(String.format("An error occurred while parsing the response [ url :: %s ]", request.getURI().toString()), e);
            return Optional.empty();
        } finally {
            HttpClientUtils.closeQuietly(response);
        }

        return Optional.of(responseBody);
    }


    private Optional<String> get(final String url) {

        return request(new HttpGet(url));
    }


    private Optional<String> post(final String url) {
        return post(url, Optional.empty());
    }


    private Optional<String> post(final String url, final Optional<HttpEntity> entity) {

        final HttpPost post = new HttpPost(url);
        entity.ifPresent(post::setEntity);

        return request(post);
    }

}
