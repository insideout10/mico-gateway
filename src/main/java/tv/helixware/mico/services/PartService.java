package tv.helixware.mico.services;

import com.github.anno4j.Anno4j;
import com.github.anno4j.model.Annotation;
import com.github.anno4j.model.impl.selector.FragmentSelector;
import com.github.anno4j.model.impl.targets.SpecificResource;
import com.github.anno4j.querying.QueryService;
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
import org.openrdf.repository.sparql.SPARQLRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import com.github.anno4j.model.impl.target.SpecificResource;

/**
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PartService {

    private final Anno4j anno4j;

    private final MicoClient client;

    private final PartRepository partRepository;
    private final SequenceFragmentRepository sequenceFragmentRepository;
    private final FaceFragmentRepository faceFragmentRepository;

    @Value("${helixware.application.key}")
    private String applicationKey;

    @Value("${helixware.application.secret}")
    private String applicationSecret;

    private final static Pattern XYWH_PATTERN = Pattern.compile("#xywh=(\\d+),(\\d+),(\\d+),(\\d+)");
    private final static Pattern NPT_PATTERN = Pattern.compile("npt:(\\d+),(\\d+)");

//    /**
//     * Create an instance of the ContentPartService.
//     *
//     * @param client
//     * @since 1.0.0
//     */
//    @Autowired
//    public PartService(final MicoClient client,
//                       final PartRepository partRepository,
//                       final SequenceFragmentRepository sequenceFragmentRepository,
//                       final FaceFragmentRepository faceFragmentRepository,
//                        final String applicationKey,
//                        final String applicationSecret) {
//
//        this.client = client;
//
//        this.partRepository = partRepository;
//        this.sequenceFragmentRepository = sequenceFragmentRepository;
//        this.faceFragmentRepository = faceFragmentRepository;
//
//        this.applicationKey = applicationKey;
//        this.applicationSecret = applicationSecret;
//
//    }

    /**
     * Create a {@link Part} with the provided file.
     *
     * @param item
     * @param mimeType
     * @param name
     * @param file
     * @return
     * @since 1.0.0
     */
    public Optional<Part> create(final Item item, final String mimeType, final String name, final File file) {

        // Return the content part persisted to the database.
        return client.addContentPart(item, mimeType, name, file)
                .map(partRepository::save);

    }

    /**
     * Create a {@link Part} using the file at the specified URL.
     *
     * @param item
     * @param mimeType
     * @param name
     * @param url
     * @return
     * @since 1.0.0
     */
    public Optional<Part> create(final Item item, final String mimeType, final String name, final URL url) {

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
            final Optional<Part> contentPart = create(item, mimeType, name, tempFile);

            tempFile.delete();

            return contentPart;
        } catch (IOException | URISyntaxException e) {
            log.error("An error occurred", e);
        }

        return Optional.empty();

    }

    /**
     * Process the {@link Part} by saving the related annotations.
     *
     * @param part
     * @since 1.0.0
     */
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

        /**
         * Temporal Video Segmentation:
         *  * TVSShotBoundaryFrameBody
         *
         * Face Detection:
         *  * FaceDetectionBody
         */

        /**
         * Speech-To-Text
         * QueryService queryService = anno4j
         .createQueryService(Annotation.class)
         .addPrefix("mico", "http://www.mico-project.eu/ns/platform/1.0/schema#")
         .setAnnotationCriteria("oa:hasBody[is-a mico:STTBody]")
         .setAnnotationCriteria("^oa:hasContent/^oa:hasContentPart", contentItem);
         */

//        QueryService queryService = Anno4j.getInstance()
//                .createQueryService(Annotation.class)
//                .addPrefix("mico", "http://www.mico-project.eu/ns/platform/1.0/schema#")
//                .setAnnotationCriteria("oa:hasBody[is-a mico:STTBody]")
//                .setAnnotationCriteria("^oa:hasContent/^oa:hasContentPart", part.getUuid());
//
//        final List<Annotation> annotations = queryService.execute();
//
//        log.debug("execute over");
//
//        annotations.stream().forEach(a -> log.debug(a.toString()));
//
//        log.debug("over");

//        QueryService queryService = Anno4j.getInstance()
//                .createQueryService(Annotation.class)
//                .addPrefix("mico", "http://www.mico-project.eu/ns/platform/1.0/schema#")
//                .setAnnotationCriteria("oa:hasBody[is-a mico:STTBody]")
//                .setAnnotationCriteria("^oa:hasContent/^oa:hasContentPart", contentItem);

//        query(part, "STTBody").stream()
//                .forEach(a -> log.debug(a.toString()));

        final Pattern pattern = Pattern.compile("t=npt:(\\d+),(\\d+)");

        // Get the temporal video segmentation fragments.
        query(part, "TVSShotBoundaryFrameBody").stream()
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
                        final SequenceFragment fragment = new SequenceFragment(Long.valueOf(matcher.group(1)), Long.valueOf(matcher.group(2)), part);
                        sequenceFragmentRepository.save(fragment);
                    }
                });

        // Running the prototype for insideout
        query(part, "FaceDetectionBody").forEach(a -> {
            log.info(String.format("FaceAnnotation :: %s", a.toString()));

            final SpecificResource target = (SpecificResource) a.getTargets().stream().findFirst().get();
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

    }

    private List<Annotation> query(final Part part, final String bodyType) throws RepositoryConfigException, RepositoryException, ParseException, MalformedQueryException, QueryEvaluationException {

//        final Anno4j anno4j = Anno4j.getInstance();

        // Configuring the repository for Anno4j, but using the default Anno4j IDGenerator
        final SPARQLRepository repository = new SPARQLRepository("http://" + client.getServer() + "/marmotta/sparql/select", "http://" + client.getServer() + "/marmotta/sparql/update");
        repository.setUsernameAndPassword(client.getUsername(), client.getPassword());
        repository.initialize();
        anno4j.setRepository(repository);

        final String contentPartId = part.getUri();
        final String ldPath = "^mico:hasContent/^mico:hasContentPart/mico:hasContentPart";

        // Getting the QueryService to query for Annotation objects, setting the mico namespace and adding a criteria.
        final QueryService queryService = createQueryService()
                .addCriteria(ldPath, contentPartId)
                .addCriteria("[is-a mico:" + bodyType + "]");
//                .setAnnotationCriteria(ldPath, contentPartId)
//                .setBodyCriteria("[is-a mico:" + bodyType + "]");

        // executing the query
        return queryService.execute(Annotation.class);
    }

    private List<FragmentSelector> getTemporalFragment(final Annotation faceAnnotation) throws RepositoryException, QueryEvaluationException, MalformedQueryException, ParseException, RepositoryConfigException {

        final List<Annotation> anShot = createQueryService()
                .addCriteria("oa:hasBody[is-a mico:TVSShotBoundaryFrameBody] | oa:hasBody[is-a mico:TVSKeyFrameBody]")
                .addCriteria("^mico:hasContent/^dct:source/^oa:hasSource/^oa:hasTarget", faceAnnotation.getResource().toString())
//                .setAnnotationCriteria("oa:hasBody[is-a mico:TVSShotBoundaryFrameBody] | oa:hasBody[is-a mico:TVSKeyFrameBody]")
//                .setAnnotationCriteria("^mico:hasContent/^dct:source/^oa:hasSource/^oa:hasTarget", faceAnnotation.getResource().toString())
                .execute(Annotation.class);

        final List<FragmentSelector> results = new ArrayList<>();
        for (final Annotation anno : anShot) {
            val target = anno.getTargets().stream().findFirst().get();
            results.add((FragmentSelector) ((SpecificResource) target).getSelector());
//            results.add((FragmentSelector) ((SpecificResource) anno.getTarget()).getSelector());
        }

        return results;
    }

    /**
     * Create a {@link QueryService} instance and configure it to the {@link Annotation} class with the default prefixes.
     *
     * @return A {@link QueryService} instance.
     * @since 1.0.0
     */
    private QueryService createQueryService() throws RepositoryConfigException, RepositoryException {

//        final Anno4j anno4j = Anno4j.getInstance();

        return anno4j.createQueryService()
                .addPrefix("mico", "http://www.mico-project.eu/ns/platform/1.0/schema#")
                .addPrefix("dct", "http://purl.org/dc/terms/");

    }

}
