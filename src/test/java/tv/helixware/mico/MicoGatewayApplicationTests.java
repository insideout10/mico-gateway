package tv.helixware.mico;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MicoGatewayApplication.class, webEnvironment = RANDOM_PORT)
public class MicoGatewayApplicationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void exampleTest() {

        String body = this.restTemplate.getForObject("/", String.class);
        assertThat(body).isEqualTo("Hello World");

    }

}
