package tv.helixware.mico.services;

import com.github.anno4j.Anno4j;
import com.github.anno4j.model.Annotation;
import com.github.anno4j.model.impl.selector.FragmentSelector;
import com.github.anno4j.model.impl.target.SpecificResource;
import com.github.anno4j.querying.QueryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.marmotta.ldpath.parser.ParseException;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.sparql.SPARQLRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tv.helixware.mico.ContentItem;
import tv.helixware.mico.MicoClient;
import tv.helixware.mico.model.ContentPart;
import tv.helixware.mico.model.Fragment;
import tv.helixware.mico.persist.ContentPartRepository;
import tv.helixware.mico.persist.FragmentRepository;
import tv.helixware.mico.response.CheckStatusResponse;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @since 1.0.0
 */
@Slf4j
@Service
public class IngestionService {

    private final MicoClient client;

    private final ContentPartRepository contentPartRepository;
    private final FragmentRepository fragmentRepository;

    /**
     * Create an instance of the IngestionService.
     *
     * @since 1.0.0
     */
    @Autowired
    public IngestionService(final MicoClient micoClient, final ContentPartRepository contentPartRepository, final FragmentRepository fragmentRepository) {

        this.client = micoClient;

        this.contentPartRepository = contentPartRepository;
        this.fragmentRepository = fragmentRepository;

    }

    public void upload(final File file) {

        final String type = "video/mp4";
        final String name = RandomStringUtils.randomAlphanumeric(12) + ".mp4";

//        final MicoClient client = new MicoClient(serverURL + "broker/");

        // 1. Create a Content Item.
        client.create().ifPresent(ci -> {
            log.info(String.format("Content Item created [ content item :: %s ]", ci.getUri()));

//            final File file = new File(getClass().getClassLoader().getResource(filename).getFile());

            // 2. Add a Content Part.
            client.addContentPart(ci, type, name, file).ifPresent(cp -> {
                log.info(String.format("Content Part created [ content part :: %s ]", cp.getUri()));

                // 3. Submit the Content Item.
                final boolean result = client.submit(ci);
                log.info(String.format("Content Part submitted [ success :: %s ]", result ? "true" : "false"));

                checkStatus(client, ci);

                try {
                    getAnnotations(cp);
                } catch (RepositoryException | RepositoryConfigException | QueryEvaluationException | MalformedQueryException | ParseException e) {
                    log.error(e.getMessage(), e);
                }
            });
        });
    }

    private void checkStatus(final MicoClient client, final ContentItem contentItem) {

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

    private void getAnnotations(final ContentPart contentPart) throws RepositoryException, RepositoryConfigException, ParseException, MalformedQueryException, QueryEvaluationException {
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

        final ContentPart cp = contentPartRepository.save(contentPart);
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
                        final Fragment fragment = new Fragment(Long.valueOf(matcher.group(1)), Long.valueOf(matcher.group(2)), cp);
                        fragmentRepository.save(fragment);
                    }
                });
//        }

        // http://demo2.mico-project.eu:8080/broker/status/download?partUri=http://demo2.mico-project.eu:8080/marmotta/956fe00a-d284-4661-9dff-749c94ea8795/0a50acf2-656c-4933-a3f4-2a21c413b5d2&itemUri=http://demo2.mico-project.eu:8080/marmotta/956fe00a-d284-4661-9dff-749c94ea8795

//        final String downloadURL = String.format("http://%s:%s@%s/broker/status/download?partUri=%s&itemUri=%s", username, password, server, URLEncoder.encode(contentPart.getUri(), "UTF-8"), URLEncoder.encode(contentItem.getUri(), "UTF-8"));

    }
}
