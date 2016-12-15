package tv.helixware.mico.services;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import tv.helixware.mico.MicoGatewayApplication;
import tv.helixware.mico.model.*;
import tv.helixware.mico.persist.*;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * Test the {@link EntityMentionService}.
 *
 * @since 0.2.0
 */
@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MicoGatewayApplication.class)
public class EntityMentionServiceTest {

    /**
     * An {@link EntityMentionService} instance to test.
     *
     * @since 0.2.0
     */
    @Autowired
    private EntityMentionService entityMentionService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private PartRepository partRepository;

    @Autowired
    private FragmentRepository<Fragment> fragmentRepository;

    @Autowired
    private TopicFragmentRepository topicFragmentRepository;

    @Autowired
    private EntityFragmentRepository entityFragmentRepository;

    /**
     * Test querying the remote MICO install for entity mentions ({@link TopicFragment}s and {@link EntityFragment}s).
     *
     * @throws Exception
     * @since 0.2.0
     */
    @Test
    public void query() throws Exception {

        // Create a mock item.
        val item = itemRepository.save(new Item(null, "http://demo2.mico-project.eu:8080/marmotta/de12b016-17d1-4613-85b9-055b797d1fa3", "de12b016-17d1-4613-85b9-055b797d1fa3"));

        // Create a mock part.
        val part = partRepository.save(new Part(item, "http://demo2.mico-project.eu:8080/marmotta/e6f5c01e-0498-4a8c-a958-3b723b2bad2a", "e6f5c01e-0498-4a8c-a958-3b723b2bad2a", "video/mp4", "Lorem Ipsum"));

        // Get the entity mentions for the part.
        fragmentRepository.save(entityMentionService.query(part));

        assertEquals(3, topicFragmentRepository.count());
        assertEquals(6, entityFragmentRepository.count());

    }

}
