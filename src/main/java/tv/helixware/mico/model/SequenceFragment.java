package tv.helixware.mico.model;

import lombok.Data;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Represents a temporal video sequence fragment.
 *
 * @since 0.1.0
 */
@Data
@Entity
@DiscriminatorValue("sequence")
public class SequenceFragment extends Fragment {

    protected SequenceFragment() {
        super();
    }

    /**
     * Create an instance of the Sequence Fragment.
     *
     * @param start The start time in seconds.
     * @param end   The end time in seconds.
     * @param part  The {@link Part} this fragments refers to.
     * @since 0.1.0
     */
    public SequenceFragment(Long start, Long end, Part part) {

        super(start, end, part);

    }

}
