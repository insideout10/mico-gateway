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

    private String type;

}
