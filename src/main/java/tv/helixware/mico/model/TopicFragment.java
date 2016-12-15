package tv.helixware.mico.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Represents a _topic_ found by MICO.
 *
 * @since 0.2.0
 */
@Data
@DiscriminatorValue("topic")
@Entity
@NoArgsConstructor
public class TopicFragment extends Fragment {

    /**
     * The topic's label.
     *
     * @since 0.2.0
     */
    private String label;

    /**
     * The topic's URI reference.
     *
     * @since 0.2.0
     */
    private String reference;

    /**
     * The topic's confidence score.
     *
     * @since 0.2.0
     */
    private double confidence;

    /**
     * Create a {@link TopicFragment} instance.
     *
     * @param label      The topic's label.
     * @param reference  The topic's URI reference.
     * @param confidence The topic's confidence score.
     * @param part       The topic's {@link Part}.
     * @since 0.2.0
     */
    public TopicFragment(String label, String reference, double confidence, Part part) {
        super(-1L, -1L, part);

        this.label = label;
        this.reference = reference;
        this.confidence = confidence;

    }

}
