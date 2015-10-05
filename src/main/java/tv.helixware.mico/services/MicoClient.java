package tv.helixware.mico.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
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
import tv.helixware.mico.model.ContentItem;
import tv.helixware.mico.model.ContentPart;
import tv.helixware.mico.response.CheckStatusResponse;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @since 1.0.0
 */
@Slf4j
@Component
public class MicoClient {

    private final String server;
    private final String username;
    private final String password;
    private final String path;

    private final String serverURL;

    private final ObjectMapper objectMapper;

    private final static String INJECT_CREATE_PATH = "inject/create";
    private final static String INJECT_ADD_PATH = "inject/add";
    private final static String INJECT_SUBMIT_PATH = "inject/submit";
    private final static String STATUS_ITEMS_PATH = "status/items";


    /**
     * Create an instance of the IngestionService.
     *
     * @param server   The MICO server name.
     * @param username The username to access the server.
     * @param password The password to access the server.
     * @since 1.0.0
     */
    @Autowired
    public MicoClient(@Value("${mico.server}") final String server, @Value("${mico.path:broker/}") final String path, @Value("${mico.username}") final String username, @Value("${mico.password}") final String password) {

        this.server = server;
        this.path = path;
        this.username = username;
        this.password = password;

        this.serverURL = String.format("http://%s:%s@%s/%s", username, password, server, path);

        this.objectMapper = new ObjectMapper();
    }

    public String getServer() {
        return server;
    }

    public String getPath() {
        return path;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    /**
     * Create a {@link ContentItem}.
     *
     * @return
     * @since 1.0.0
     */
    public Optional<ContentItem> create(final Asset asset) {

        final String url = serverURL + INJECT_CREATE_PATH;

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
            if (!node.has("uri")) {
                log.error(String.format("The JSON is invalid [ url :: %s ][ response body :: %s ]", url, responseBody));
                return Optional.empty();
            }

            // Get the URI and create a new ContentItem.
            final String uri = node.get("uri").asText();
            return Optional.of(createContentItem(asset, uri));

        } catch (Exception e) {
            log.error(String.format("An error occurred while parsing the response [ url :: %s ]", url));
        }

        return Optional.empty();
    }

    /**
     * Add a {@link ContentPart} to a {@link ContentItem}.
     *
     * @param contentItem
     * @param mimeType
     * @param name
     * @return
     * @since 1.0.0
     */
    public Optional<ContentPart> addContentPart(final ContentItem contentItem, final String mimeType, final String name, final File file) {

        try {
            // Build the URI and get the response.
            final URI uri = new URIBuilder(serverURL + INJECT_ADD_PATH)
                    .setParameter("ci", contentItem.getUri())
                    .setParameter("type", mimeType)
                    .setParameter("name", name)
                    .build();

            final FileEntity entity = new FileEntity(file);

            final Optional<String> response = post(uri.toString(), Optional.of(entity));

            // If the response is empty, we return an empty.
            if (!response.isPresent())
                return Optional.empty();

            final JsonNode node = objectMapper.readTree(response.get());

            // If the *uri* field is missing from the JSON return an empty.
            if (!node.has("uri")) {
                log.error(String.format("The JSON is invalid [ url :: %s ][ response body :: %s ]", uri, response.get()));
                return Optional.empty();
            }

            // Get the URI and create a new ContentItem.
            return Optional.of(createContentPart(contentItem, node.get("uri").asText(), mimeType, name));

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
//     * @since 1.0.0
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
     * Submit the {@link ContentItem} for processing.
     *
     * @param contentItem
     * @since 1.0.0
     */
    public boolean submit(final ContentItem contentItem) {

        // Build the URI and get the response.
        try {
            final URI uri = new URIBuilder(serverURL + INJECT_SUBMIT_PATH)
                    .setParameter("ci", contentItem.getUri())
                    .build();

            // If the response is present (although empty), it's a success.
            return post(uri.toString()).isPresent();

        } catch (URISyntaxException e) {
            log.error(String.format("An error occurred while submitting a Content Item [ url :: %s ]", serverURL + INJECT_SUBMIT_PATH));
        }

        return false;
    }

    /**
     * @param contentItem
     * @param parts
     * @return
     * @since 1.0.0
     */
    public List<CheckStatusResponse> checkStatus(final ContentItem contentItem, final boolean parts) {

        try {
            // Build the URI and get the response.
            final URI uri = new URIBuilder(serverURL + STATUS_ITEMS_PATH)
                    .setParameter("uri", contentItem.getUri())
                    .setParameter("parts", parts ? "true" : "false")
                    .build();

            final Optional<String> response = get(uri.toString());

            // If the response is empty, return empty.
            if (!response.isPresent())
                return Collections.emptyList();

            // Map the response to a CheckStatusResponse.
            return objectMapper.readValue(response.get(), new TypeReference<List<CheckStatusResponse>>() {
            });

        } catch (Exception e) {
            log.error(String.format("An error occurred while parsing a response [ url :: %s ]", serverURL + STATUS_ITEMS_PATH), e);
        }

        return Collections.emptyList();
    }

    // TODO: move to a ContentItemBuilder class.

    /**
     * Create a {@link ContentItem} given a URI.
     *
     * @param uri The ContentItem URI as returned by MICO.
     * @return A ContentItem instance
     * @since 1.0.0
     */
    private ContentItem createContentItem(final Asset asset, final String uri) {

        final String[] parts = uri.split("/");
        return new ContentItem(asset, uri, parts[parts.length - 1]);
    }

    // TODO: move to a ContentPartBuilder class.

    /**
     * Create a {@link ContentPart} given a URI.
     *
     * @param uri The ContentPart URI as returned by MICO.
     * @return A ContentPart instance.
     * @since 1.0.0
     */
    private ContentPart createContentPart(final ContentItem contentItem, final String uri, final String mimeType, final String name) {

        final String[] parts = uri.split("/");
        return new ContentPart(contentItem, uri, parts[parts.length - 1], mimeType, name);
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
