package tv.helixware.mico.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Represents an entity discovered by MICO. This class extends {@link TopicFragment} since it has all the properties,
 * plus the entity type.
 *
 * @since 0.2.0
 */
@Data
@DiscriminatorValue("entity")
@Entity
@NoArgsConstructor
// Even though the Entity Fragment shares many properties with the TopicFragment (being a de facto extended version of it),
// we extend the base abstract Fragment class in order to avoid the TopicFragmentRepository returning TopicFragments and
// EntityFragments all together.
public class EntityFragment extends Fragment {

    /**
     * The entity's label.
     *
     * @since 0.2.0
     */
    private String label;

    /**
     * The entity's URI reference.
     *
     * @since 0.2.0
     */
    private String reference;

    /**
     * The entity's confidence score.
     *
     * @since 0.2.0
     */
    private double confidence;

    /**
     * The entity's type URI.
     *
     * @since 0.2.0
     */
    private String type;

    /**
     * Create an {@link EntityFragment} instance.
     *
     * @param label      The entity's label.
     * @param reference  The entity's URI reference.
     * @param type       The entity's type URI.
     * @param confidence The entity's confidence score.
     * @param part       The entity's {@link Part}.
     * @since 0.2.0
     */
    public EntityFragment(String label, String reference, String type, double confidence, Part part) {
        super(-1L, -1L, part);

        this.label = label;
        this.reference = reference;
        this.confidence = confidence;
        this.type = type;

    }

}
