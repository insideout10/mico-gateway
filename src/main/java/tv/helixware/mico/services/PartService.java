package tv.helixware.mico.services;

import com.github.anno4j.model.Annotation;
import com.github.anno4j.model.impl.selector.FragmentSelector;
import com.github.anno4j.model.impl.targets.SpecificResource;
import com.github.anno4j.querying.QueryService;
import eu.mico.platform.anno4j.model.PartMMM;
import eu.mico.platform.anno4j.model.impl.targetmmm.SpecificResourceMMM;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tv.helixware.mico.factories.QueryServiceFactory;
import tv.helixware.mico.helpers.AnnotationHelper;
import tv.helixware.mico.model.FaceFragment;
import tv.helixware.mico.model.Item;
import tv.helixware.mico.model.Part;
import tv.helixware.mico.model.SequenceFragment;
import tv.helixware.mico.persist.FaceFragmentRepository;
import tv.helixware.mico.persist.PartRepository;
import tv.helixware.mico.persist.SequenceFragmentRepository;
import tv.helixware.mico.response.CheckStatusResponse;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @since 0.1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PartService {

    /**
     * A {@link QueryServiceFactory} instance to create {@link QueryService}s.
     *
     * @since 0.2.0
     */
    private final QueryServiceFactory queryServiceFactory;

    private final MicoClient client;

    private final PartRepository partRepository;
    private final SequenceFragmentRepository sequenceFragmentRepository;
    private final FaceFragmentRepository faceFragmentRepository;

    @Value("${helixware.application.key}")
    private String applicationKey;

    @Value("${helixware.application.secret}")
    private String applicationSecret;

    private final static Pattern XYWH_PATTERN = Pattern.compile("#xywh=(\\d+),(\\d+),(\\d+),(\\d+)");
    private final static Pattern NPT_PATTERN = Pattern.compile("npt:(\\d+)(?:\\.\\d+)?,(\\d+)(?:\\.\\d+)?");

    /**
     * Create a {@link Part} with the provided file.
     *
     * @param item
     * @param micoType
     * @param name
     * @param file
     * @return
     * @since 0.1.0
     */
    public Optional<Part> create(final Item item, final String micoType, final String mimeType, final String name, final File file) {

        // Return the content part persisted to the database.
        return client.addContentPart(item, micoType, mimeType, name, file)
                .map(partRepository::save);

    }

    /**
     * Create a {@link Part} using the file at the specified URL.
     *
     * @param item
     * @param micoType
     * @param name
     * @param url
     * @return
     * @since 0.1.0
     */
    public Optional<Part> create(final Item item, final String micoType, final String mimeType, final String name, final URL url) {

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

            val part = create(item, micoType, mimeType, name, tempFile);

            tempFile.delete();

            // Add some trace logging, just in case.
            if (log.isDebugEnabled()) {
                if (part.isPresent())
                    log.debug(String.format("Part created [ uri :: %s ]", part.get().getUri()));
                else
                    log.debug("Part not created");
            }

            return part;

        } catch (IOException | URISyntaxException e) {
            log.error("An error occurred", e);
        }

        // If we got here something went wrong.
        log.error("Error creating part");

        return Optional.empty();

    }

    /**
     * Process the {@link Part} by saving the related annotations.
     *
     * @param part
     * @since 0.1.0
     */
    @Transactional
    public void process(final Part part) {

        blockUntilComplete(client, part.getItem());

        try {
            getAnnotationsInternal(part);
        } catch (RepositoryException | RepositoryConfigException | QueryEvaluationException | MalformedQueryException | ParseException e) {
            log.error(e.getMessage(), e);
        }
    }


    private void blockUntilComplete(final MicoClient client, final Item item) {

        List<CheckStatusResponse> response;
        while ((response = client.checkStatus(item, true)).isEmpty() || !response.get(0).isFinished()) {
            log.info(String.format("Not finished, waiting..."));

            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        log.info("Content Part is finished");

    }

    private void getAnnotationsInternal(final Part part) throws RepositoryException, RepositoryConfigException, ParseException, MalformedQueryException, QueryEvaluationException {

        // Annotations > Targets(/Resources) -> Selectors

        // In the following code, we collect to list each time in order to have some debugging data and understand how
        // many of each annotation, target, resource, selector we get from the query.

        // Get the temporal video segmentation fragments.
        val annotations = query(part, "TVSShotBoundaryFrameBody");

        // Get the targets.
        val targets = annotations.stream()
                .flatMap(a -> a.getTarget().stream())
                .collect(Collectors.toList());

        // Get the resources associated with the annotations.
        val resources = targets.stream()
                .filter(t -> t instanceof SpecificResourceMMM)
                .map(t -> (SpecificResourceMMM) t)
                .collect(Collectors.toList());

        // Get the selectors from the resources.
        val selectors = resources.stream()
                .filter(r -> r.getSelector() instanceof FragmentSelector)
                .map(r -> (FragmentSelector) r.getSelector())
                .collect(Collectors.toList());

        log.debug(String.format("Found %d annotation(s), %d target(s), %d resource(s) and %d selector(s)", annotations.size(), targets.size(), resources.size(), selectors.size()));

        // For each selector, save a fragment.
        selectors.stream()
                .map(FragmentSelector::getValue)
                .distinct()
                .forEach(v -> saveFragment(v, part));

        // Running the prototype for insideout
        query(part, "FaceDetectionBody").forEach(a -> {

            log.info(String.format("FaceAnnotation :: %s", a.toString()));

            a.getTarget().stream()
                    .map(t -> (SpecificResource) t)
                    .forEach(target -> {
                        // final SpecificResource target = (SpecificResource) AnnotationHelper.target(a);

                        final FragmentSelector selector = (FragmentSelector) target.getSelector();
                        log.info(String.format(" * Selector :: %s", selector));

                        final Matcher xywhMatcher = XYWH_PATTERN.matcher(selector.getValue());

                        if (xywhMatcher.find()) {

                            final Long x = Long.valueOf(xywhMatcher.group(1));
                            final Long y = Long.valueOf(xywhMatcher.group(2));
                            final Long w = Long.valueOf(xywhMatcher.group(3));
                            final Long h = Long.valueOf(xywhMatcher.group(4));

                            // This is required only when the server is configured with Face Detection (video-keyframes).
                            try {
                                getTemporalFragment(a).forEach(f -> {

                                    final Matcher nptMatcher = NPT_PATTERN.matcher(f.getValue());

                                    if (nptMatcher.find()) {
                                        final Long start = Long.valueOf(nptMatcher.group(1));
                                        final Long end = Long.valueOf(nptMatcher.group(2));

                                        faceFragmentRepository.save(new FaceFragment(start, end, x, y, w, h, part));

                                        log.info(String.format(" ** TemporalFragment :: %s ", f));
                                    }
                                });
                            } catch (RepositoryConfigException | RepositoryException | QueryEvaluationException | MalformedQueryException | ParseException e) {
                                e.printStackTrace();
                            }

                        }
                    });

        });

    }

    /**
     * Parse the value using the NPT pattern and save a {@link SequenceFragment}
     *
     * @param value The NPT string value.
     * @param part  The {@link Part} owner of the {@link SequenceFragment}.
     * @since 0.2.0
     */
    private void saveFragment(final String value, final Part part) {

        val matcher = NPT_PATTERN.matcher(value);

        if (!matcher.find()) {

            log.info(String.format("No match found [ value :: %s ]", value));
            return;
        }

        log.info(String.format("[ start :: %s ][ end :: %s ]", matcher.group(1), matcher.group(2)));

        val fragment = new SequenceFragment(Long.valueOf(matcher.group(1)), Long.valueOf(matcher.group(2)), part);
        sequenceFragmentRepository.save(fragment);

    }

    private List<PartMMM> query(final Part part, final String bodyType) throws RepositoryConfigException, RepositoryException, ParseException, MalformedQueryException, QueryEvaluationException {

        val contentPartId = part.getUri();

        // Getting the QueryService to query for Annotation objects, setting the mico namespace and adding a criteria.
        final QueryService queryService = queryServiceFactory.create()
                .addCriteria("^mmm:hasPart", contentPartId)
                .addCriteria("mmm:hasBody[is-a mmmterms:" + bodyType + "]");

        try {

            return queryService.execute(PartMMM.class);

        } catch (Exception e) {

            log.error(String.format("An error occurred querying the remote service [ 1st criteria :: %s, %s ][ 2nd criteria :: %s ]", "^mmm:hasPart", contentPartId, "[is-a mmmterms:" + bodyType + "]"), e);

        }

        // An error occurred if we got here, let's return an empty list then.
        return Collections.emptyList();
    }

    private List<FragmentSelector> getTemporalFragment(final Annotation faceAnnotation) throws RepositoryException, QueryEvaluationException, MalformedQueryException, ParseException, RepositoryConfigException {

        final List<PartMMM> parts = queryServiceFactory.create()

                .addCriteria("^mmm:hasPart", faceAnnotation.getResource().toString()) // Where itemID is the ID of your item
                .addCriteria("mmm:hasBody[is-a mmmterms:TVSShotBoundaryFrameBody] | mmm:hasBody[is-a mmmterms:TVSKeyFrameBody]")

//                .addCriteria("mmm:hasBody[is-a mmmterms:TVSShotBoundaryFrameBody] | mmmterms:hasBody[is-a mmmterms:TVSKeyFrameBody]")
//                .addCriteria("^mmm:hasContent/^dct:source/^oa:hasSource/^oa:hasTarget", faceAnnotation.getResource().toString())
//                .setAnnotationCriteria("oa:hasBody[is-a mico:TVSShotBoundaryFrameBody] | oa:hasBody[is-a mico:TVSKeyFrameBody]")
//                .setAnnotationCriteria("^mico:hasContent/^dct:source/^oa:hasSource/^oa:hasTarget", faceAnnotation.getResource().toString())
                .execute(PartMMM.class);

        final List<FragmentSelector> results = new ArrayList<>();
        for (final Annotation annotation : parts) {
            final SpecificResource resource = (SpecificResource) AnnotationHelper.target(annotation);
            results.add((FragmentSelector) resource.getSelector());
//            results.add((FragmentSelector) ((SpecificResource) anno.getTarget()).getSelector());
        }

        return results;
    }

//    /**
//     * Create a {@link QueryService} instance and configure it to the {@link Annotation} class with the default prefixes.
//     *
//     * @return A {@link QueryService} instance.
//     * @since 0.1.0
//     */
//    private QueryService createQueryService() throws RepositoryConfigException, RepositoryException {
//
////        final Anno4j anno4j = Anno4j.getInstance();
//
//        return anno4j.createQueryService()
//                .addPrefix(MMM.PREFIX, MMM.NS)
//                .addPrefix(MMMTERMS.PREFIX, MMMTERMS.NS)
//                .addPrefix("mico", "http://www.mico-project.eu/ns/platform/1.0/schema#")
//                .addPrefix("dct", "http://purl.org/dc/terms/");
//
//    }

}
