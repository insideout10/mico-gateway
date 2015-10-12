package tv.helixware.mico.model;

import lombok.Data;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Represents a video fragment with a face.
 *
 * @since 1.0.0
 */
@Data
@Entity
@DiscriminatorValue("face")
public class FaceFragment extends Fragment {

    private Long x;
    private Long y;
    private Long width;
    private Long height;

    protected FaceFragment() {
        super();
    }

    public FaceFragment(Long start, Long end, Long x, Long y, Long width, Long height, Part part) {
        super(start, end, part);

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

}
