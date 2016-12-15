package tv.helixware.mico.services;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import tv.helixware.mico.MicoGatewayApplication;

/**
 * Test the {@link QueryService}.
 *
 * @since 0.2.0
 */
@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MicoGatewayApplication.class)
public class QueryServiceTest {

    @Autowired
    private QueryService queryService;

    @Test
    public void testQuery() {

        val statement = "PREFIX mmm: <http://www.mico-project.eu/ns/mmm/2.0/schema#>\n"
                + "PREFIX fam: <http://vocab.fusepool.info/fam#>\n"
                + "\n"
                + "SELECT * WHERE { {"
                + "  <http://demo2.mico-project.eu:8080/marmotta/de12b016-17d1-4613-85b9-055b797d1fa3> mmm:hasPart [ mmm:hasBody ["
                + "    a <http://vocab.fusepool.info/fam#LinkedEntity> ;"
                + "    fam:entity-label ?label ;"
                + "    fam:entity-reference ?entityReference ;"
                + "    fam:entity-type ?type ;"
                + "    fam:confidence ?confidence"
                + "  ] ]"
                + " } UNION {"
                + "  <http://demo2.mico-project.eu:8080/marmotta/de12b016-17d1-4613-85b9-055b797d1fa3> mmm:hasPart [ mmm:hasBody ["
                + "    a <http://vocab.fusepool.info/fam#TopicAnnotation> ;"
                + "    fam:topic-label ?label ;"
                + "    fam:topic-reference ?entityReference ;"
                + "    fam:confidence ?confidence"
                + "  ] ]"
                + " } }";

        log.debug(queryService.query(statement));

    }

}