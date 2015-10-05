package tv.helixware.mico.model;

import lombok.Data;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @since 1.0.0
 */
@Data
@Entity
public class ContentItem implements Serializable {

    private static final long serialVersionUID = 2L;

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, length = 1024)
    private String uri;

    @Column(nullable = false)
    private String uuid;

    @ManyToOne
    @JoinColumn(name = "asset_id")
    private Asset asset;

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

    protected ContentItem() {
    }

    /**
     * Create an instance of ContentItem with the specified UUID.
     *
     * @param uuid The unique ID for this ContentItem.
     * @since 4.2.0
     */
    public ContentItem(final Asset asset, final String uri, final String uuid) {

        this.asset = asset;
        this.uri = uri;
        this.uuid = uuid;
    }

    public String getUri() {
        return uri;
    }

    public String getUUID() {
        return uuid;
    }

    @Override
    public String toString() {
        return "ContentItem{" +
                "uri='" + uri + '\'' +
                ", uuid='" + uuid + '\'' +
                '}';
    }
}
