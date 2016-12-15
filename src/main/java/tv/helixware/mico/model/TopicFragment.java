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

    private String label;
    private String reference;
    private double confidence;

}
