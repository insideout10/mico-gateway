package tv.helixware.mico.model;

import lombok.Data;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Represents a Content Part in MICO.
 *
 * @since 1.0.0
 */
@Data
@Entity
public class ContentPart implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, length = 1024)
    private final String uri;

    @Column(nullable = false, length = 1024)
    private final String uuid;

    @Column(nullable = false)
    private final String mimeType;

    @Column(nullable = false)
    private final String name;

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

    /**
     * @param uri
     * @param uuid
     * @since 4.2.0
     */
    public ContentPart(final String uri, final String uuid, final String mimeType, final String name) {

        this.uri = uri;
        this.uuid = uuid;
        this.mimeType = mimeType;
        this.name = name;
    }

}
