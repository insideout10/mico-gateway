package tv.helixware.mico.services;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tv.helixware.mico.model.EntityFragment;
import tv.helixware.mico.model.Fragment;
import tv.helixware.mico.model.Part;
import tv.helixware.mico.model.TopicFragment;

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
     * The query statement template.
     *
     * @since 0.2.0
     */
    private final static String STATEMENT = "PREFIX mmm: <http://www.mico-project.eu/ns/mmm/2.0/schema#>\n"
            + "PREFIX fam: <http://vocab.fusepool.info/fam#>\n"
            + "\n"
            + "SELECT * WHERE { {"
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

    public List<Fragment> query(final Part part) {

        // Create the statement.
        val statement = String.format(STATEMENT, part.getItem().getUri());

        val response = queryService.query(statement, "text/tab-separated-values");

        return Arrays.stream(response.split("\n"))
                // Skip the header.
                .skip(1)
                .map((x) -> x.split("\t"))
                .map((x) -> x[2].isEmpty()
                        ? new TopicFragment(x[0], x[1], Double.valueOf(x[3]), part)
                        : new EntityFragment(x[0], x[1], x[2], Double.valueOf(x[3]), part))
                .collect(toList());
    }

}
