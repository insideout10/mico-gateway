package tv.helixware.mico.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tv.helixware.mico.model.EntityFragment;
import tv.helixware.mico.model.Fragment;
import tv.helixware.mico.model.Part;
import tv.helixware.mico.model.TopicFragment;
import tv.helixware.mico.persist.FragmentRepository;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * The {@link EntityMentionService} queries MICO for {@link TopicFragment}s and for {@link EntityFragment}s related to
 * a submitted item. In order to do so it also defines a protocol.
 *
 * @since 0.2.0
 */
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
@Slf4j
public class EntityMentionService {

    /**
     * The query statement template.
     *
     * @since 0.2.0
     */
    private final static String STATEMENT = "PREFIX mmm: <http://www.mico-project.eu/ns/mmm/2.0/schema#>\n"
            + "PREFIX fam: <http://vocab.fusepool.info/fam#>\n"
            + "\n"
            + "SELECT ?label ?entityReference ?type ?confidence WHERE { {"
            + "  <%1$s> mmm:hasPart [ mmm:hasBody ["
            + "    a <http://vocab.fusepool.info/fam#LinkedEntity> ;"
            + "    fam:entity-label ?label ;"
            + "    fam:entity-reference ?entityReference ;"
            + "    fam:entity-type ?type ;"
            + "    fam:confidence ?confidence"
            + "  ] ]"
            + " } UNION {"
            + "  <%1$s> mmm:hasPart [ mmm:hasBody ["
            + "    a <http://vocab.fusepool.info/fam#TopicAnnotation> ;"
            + "    fam:topic-label ?label ;"
            + "    fam:topic-reference ?entityReference ;"
            + "    fam:confidence ?confidence"
            + "  ] ]"
            + " } }";

    /**
     * A {@link QueryService} instance used to post queries to MICO.
     *
     * @since 0.2.0
     */
    private final QueryService queryService;

    /**
     * A {@link FragmentRepository} instance used to persist {@link EntityFragment}s and {@link TopicFragment}s.
     *
     * @since 0.2.0
     */
    private final FragmentRepository<Fragment> repository;

    /**
     * Retrieves the {@link EntityFragment}s and {@link TopicFragment}s from the remote MICO server and persist them
     * to the local data store.
     *
     * @param part The {@link Part}.
     * @since 0.2.0
     */
    public void retrieve(final Part part) {

        // Get the item URI.
        val uri = part.getItem().getUri();

        log.trace(String.format("Retrieving Entity Mentions [ uri :: %s ]", uri));

        // Create the statement.
        val statement = String.format(STATEMENT, uri);

        // Query the remote MICO instance and get the string response (which is a tsv).
        val response = queryService.query(statement, "text/tab-separated-values");

        log.trace(String.format("Received Entity Mentions response [ response :: %s ]", response));

        // Split the whole file in lines, and skip the header.
        Arrays.stream(response.split("\n")).skip(1)
                // Split each line into fields.
                .map(x -> x.split("\t"))
                // Filter out rows with no fields.
                .filter(x -> 4 == x.length)
                // Create a TopicFragment or an EntityFragment, based on whether the `type` field is set (EntityFragment)
                // or not (TopicFragment).
                .map(x -> x[2].isEmpty()
                        ? new TopicFragment(cleanLabel(x[0]), x[1], Double.valueOf(x[3]), part)
                        : new EntityFragment(cleanLabel(x[0]), x[1], x[2], Double.valueOf(x[3]), part))
                // Finally save each fragment.
                .forEach(repository::save);

    }

    private final static Pattern CLEAN_LABEL = Pattern.compile("^(?:\"(?=(.*?)(?:\"@\\w{2}$))|(.*)$)");

    private String cleanLabel(final String label) {

        val matcher = CLEAN_LABEL.matcher(label);

        if (!matcher.matches()) return label;

        return null != matcher.group(1) ? matcher.group(1) : matcher.group(2);
    }

}
