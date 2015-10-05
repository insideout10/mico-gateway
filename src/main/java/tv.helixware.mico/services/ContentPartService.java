package tv.helixware.mico.services;

import com.github.anno4j.Anno4j;
import com.github.anno4j.model.Annotation;
import com.github.anno4j.model.impl.selector.FragmentSelector;
import com.github.anno4j.model.impl.target.SpecificResource;
import com.github.anno4j.querying.QueryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.marmotta.ldpath.parser.ParseException;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.sparql.SPARQLRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tv.helixware.mico.model.ContentItem;
import tv.helixware.mico.model.ContentPart;
import tv.helixware.mico.model.Fragment;
import tv.helixware.mico.persist.ContentPartRepository;
import tv.helixware.mico.persist.FragmentRepository;
import tv.helixware.mico.response.CheckStatusResponse;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @since 1.0.0
 */
@Slf4j
@Service
public class ContentPartService {

    private final MicoClient client;

    private final ContentPartRepository contentPartRepository;
    private final FragmentRepository fragmentRepository;

    private final String applicationKey;
    private final String applicationSecret;

    /**
     * Create an instance of the ContentPartService.
     *
     * @param client
     * @since 1.0.0
     */
    @Autowired
    public ContentPartService(final MicoClient client, final ContentPartRepository contentPartRepository, final FragmentRepository fragmentRepository, @Value("${helixware.application.key}") final String applicationKey, @Value("${helixware.application.secret}") final String applicationSecret) {

        this.client = client;

        this.contentPartRepository = contentPartRepository;
        this.fragmentRepository = fragmentRepository;

        this.applicationKey = applicationKey;
        this.applicationSecret = applicationSecret;

    }

    /**
     * Create a {@link ContentPart} with the provided file.
     *
     * @param contentItem
     * @param mimeType
     * @param name
     * @param file
     * @return
     * @since 1.0.0
     */
    public Optional<ContentPart> create(final ContentItem contentItem, final String mimeType, final String name, final File file) {

        // Return the content part persisted to the database.
        return client.addContentPart(contentItem, mimeType, name, file)
                .map(contentPartRepository::save);

    }

    /**
     * Create a {@link ContentPart} using the file at the specified URL.
     *
     * @param contentItem
     * @param mimeType
     * @param name
     * @param url
     * @return
     * @since 1.0.0
     */
    public Optional<ContentPart> create(final ContentItem contentItem, final String mimeType, final String name, final URL url) {

        try {

            // Copy locally the remote file and create a content part.
            final File tempFile = File.createTempFile("mico-", "tmp");

            // Copy the remote file to a local temp file.
            try (final CloseableHttpClient client = HttpClients.createDefault()) {

                final HttpGet get = new HttpGet(url.toURI());

                // Set the HelixWare headers required for authentication.
                get.addHeader("X-Application-Key", applicationKey);
                get.addHeader("X-Application-Secret", applicationSecret);

                try (final CloseableHttpResponse response = client.execute(get)) {
                    FileUtils.copyInputStreamToFile(response.getEntity().getContent(), tempFile);
                }
            }
            final Optional<ContentPart> contentPart = create(contentItem, mimeType, name, tempFile);

            tempFile.delete();

            return contentPart;
        } catch (IOException | URISyntaxException e) {
            log.error("An error occurred", e);
        }

        return Optional.empty();

    }

    /**
     * Process the {@link ContentPart} by saving the related annotations.
     *
     * @param contentPart
     * @since 1.0.0
     */
    public void process(final ContentPart contentPart) {

        blockUntilComplete(client, contentPart.getContentItem());

        try {
            getAnnotationsInternal(contentPart);
        } catch (RepositoryException | RepositoryConfigException | QueryEvaluationException | MalformedQueryException | ParseException e) {
            log.error(e.getMessage(), e);
        }
    }


    private void blockUntilComplete(final MicoClient client, final ContentItem contentItem) {

        List<CheckStatusResponse> response;
        while ((response = client.checkStatus(contentItem, true)).isEmpty() || !response.get(0).isFinished()) {
            log.info(String.format("Not finished, waiting..."));

            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        log.info("Content Part is finished");

    }

    private void getAnnotationsInternal(final ContentPart contentPart) throws RepositoryException, RepositoryConfigException, ParseException, MalformedQueryException, QueryEvaluationException {
        final Anno4j anno4j = Anno4j.getInstance();
        // Configuring the repository for Anno4j, but using the default Anno4j IDGenerator

        final SPARQLRepository repository = new SPARQLRepository("http://" + client.getServer() + "/marmotta/sparql/select", "http://" + client.getServer() + "/marmotta/sparql/update");
        repository.setUsernameAndPassword(client.getUsername(), client.getPassword());
        repository.initialize();
        anno4j.setRepository(repository);

        final String contentPartId = contentPart.getUri();
        final String ldPath = "^mico:hasContent/^mico:hasContentPart/mico:hasContentPart";

        // Getting the QueryService to query for Annotation objects, setting the mico namespace and adding a criteria.
        final QueryService queryService = anno4j
                .createQueryService(Annotation.class)
                .addPrefix("mico", "http://www.mico-project.eu/ns/platform/1.0/schema#")
//                .setAnnotationCriteria(ldPath, contentPartId, Comparison.EQ);
                .setAnnotationCriteria(ldPath, contentPartId);


        // Running the prototype for insideout
        final List<Annotation> annotationList = queryService.execute();

        log.info("List of queried annotation objects: \r\n");

        log.info(annotationList.size() + " Annotation objects found");
//        for (Annotation an : annotationList) {

//            logger.info("Current annotation object: \r\n {}", an.toString());

//            log.info("Current annotation object: \r\n {}", an.toString());

        final Pattern pattern = Pattern.compile("t=npt:(\\d+),(\\d+)");

        annotationList.stream()
                .flatMap(a -> a.getTargets().stream())
                .filter(t -> t instanceof SpecificResource)
                .map(t -> (SpecificResource) t)
                .filter(r -> r.getSelector() instanceof FragmentSelector)
                .map(r -> (FragmentSelector) r.getSelector())
                .map(FragmentSelector::getValue)
                .distinct()
                .forEach(v -> {
                    final Matcher matcher = pattern.matcher(v);
                    if (matcher.find()) {
                        log.info(String.format("[ start :: %s ][ end :: %s ]", matcher.group(1), matcher.group(2)));
                        final Fragment fragment = new Fragment(Long.valueOf(matcher.group(1)), Long.valueOf(matcher.group(2)), contentPart);
                        fragmentRepository.save(fragment);
                    }
                });
//        }

        // http://demo2.mico-project.eu:8080/broker/status/download?partUri=http://demo2.mico-project.eu:8080/marmotta/956fe00a-d284-4661-9dff-749c94ea8795/0a50acf2-656c-4933-a3f4-2a21c413b5d2&itemUri=http://demo2.mico-project.eu:8080/marmotta/956fe00a-d284-4661-9dff-749c94ea8795

//        final String downloadURL = String.format("http://%s:%s@%s/broker/status/download?partUri=%s&itemUri=%s", username, password, server, URLEncoder.encode(contentPart.getUri(), "UTF-8"), URLEncoder.encode(contentItem.getUri(), "UTF-8"));

    }

}
