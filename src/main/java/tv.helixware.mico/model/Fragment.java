package tv.helixware.mico.model;

import lombok.Data;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Represents a fragment identified by a start and end time (in milliseconds).
 *
 * @since 1.0.0
 */
@Data
@Entity
public class Fragment implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "start", nullable = false)
    private Long start;

    @Column(name = "end", nullable = false)
    private Long end;

    @ManyToOne
    @JoinColumn(name = "content_part_id")
    private ContentPart contentPart;

    @Version
    private Long version;

    @CreatedDate
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    @Column(name = "created_date")
    private DateTime createdDate;

    @LastModifiedDate
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    @Column(name = "last_modified_date")
    private DateTime lastModifiedDate;

    public Fragment(Long start, Long end, ContentPart contentPart) {

        this.start = start;
        this.end = end;
        this.contentPart = contentPart;
    }

}
