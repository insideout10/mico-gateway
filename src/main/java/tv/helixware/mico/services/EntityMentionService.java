package tv.helixware.mico.services;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tv.helixware.mico.model.EntityFragment;
import tv.helixware.mico.model.Fragment;
import tv.helixware.mico.model.Part;
import tv.helixware.mico.model.TopicFragment;
import tv.helixware.mico.persist.FragmentRepository;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * The {@link EntityMentionService} queries MICO for {@link TopicFragment}s and for {@link EntityFragment}s related to
 * a submitted item. In order to do so it also defines a protocol.
 *
 * @since 0.2.0
 */
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
public class EntityMentionService {

    /**
     * A {@link FragmentRepository} instance used to persist {@link EntityFragment}s and {@link TopicFragment}s.
     *
     * @since 0.2.0
     */
    private FragmentRepository<Fragment> repository;

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
     * Retrieves the {@link EntityFragment}s and {@link TopicFragment}s from the remote MICO server and persist them
     * to the local data store.
     *
     * @param part The {@link Part}.
     * @since 0.2.0
     */
    public void retrieve(final Part part) {

        // Create the statement.
        val statement = String.format(STATEMENT, part.getItem().getUri());

        // Query the remote MICO instance and get the string response (which is a tsv).
        val response = queryService.query(statement, "text/tab-separated-values");

        // Split the whole file in lines, and skip the header.
        Arrays.stream(response.split("\n")).skip(1)
                // Split each line into fields.
                .map((x) -> x.split("\t"))
                // Create a TopicFragment or an EntityFragment, based on whether the `type` field is set (EntityFragment)
                // or not (TopicFragment).
                .map((x) -> x[2].isEmpty()
                        ? new TopicFragment(x[0], x[1], Double.valueOf(x[3]), part)
                        : new EntityFragment(x[0], x[1], x[2], Double.valueOf(x[3]), part))
                // Finally save each fragment.
                .forEach(repository::save);
    }

}
