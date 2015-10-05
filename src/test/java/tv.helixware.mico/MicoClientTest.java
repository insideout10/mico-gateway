package tv.helixware.mico;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import tv.helixware.mico.services.IngestionService;

import java.io.File;

/**
 * @since 4.2.0
 */
@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = MicoGatewayApplication.class)
@WebAppConfiguration
public class MicoClientTest {

    @Autowired
    private IngestionService ingestionService;

    @Test
    public void testWildAnimals() throws Exception {

        ingestionService.upload(new File(getClass().getClassLoader().getResource("wild_animals.mp4").getFile()));

    }

    @Test
    public void testMohamed() throws Exception {

        ingestionService.upload(new File(getClass().getClassLoader().getResource("mohamed.mp4").getFile()));

    }

//    private void upload(final String filename) {
//
//        final String type = "video/mp4";
//        final String name = RandomStringUtils.randomAlphanumeric(12) + ".mp4";
//
//        final String serverURL = String.format("http://%s:%s@%s/", username, password, server);
//        final MicoClient client = new MicoClient(serverURL + "broker/");
//
//        // 1. Create a Content Item.
//        client.create().ifPresent(ci -> {
//            log.info(String.format("Content Item created [ content item :: %s ]", ci.getUri()));
//
//            final File file = new File(getClass().getClassLoader().getResource(filename).getFile());
//
//            // 2. Add a Content Part.
//            client.addContentPart(ci, type, name, file).ifPresent(cp -> {
//                log.info(String.format("Content Part created [ content part :: %s ]", cp.getUri()));
//
//                // 3. Submit the Content Item.
//                final boolean result = client.submit(ci);
//                log.info(String.format("Content Part submitted [ success :: %s ]", result ? "true" : "false"));
//
//                checkStatus(client, ci);
//
//                try {
//                    getAnnotations(cp);
//                } catch (RepositoryException | RepositoryConfigException | QueryEvaluationException | MalformedQueryException | ParseException e) {
//                    log.error(e.getMessage(), e);
//                }
//            });
//        });
//    }


}