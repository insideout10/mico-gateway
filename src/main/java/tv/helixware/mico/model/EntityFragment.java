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
public class EntityFragment extends TopicFragment {

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
        super(label, reference, confidence, part);

        this.type = type;

    }

}
