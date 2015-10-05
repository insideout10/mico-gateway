package tv.helixware.mico.model;

import lombok.Data;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;

/**
 * Represents an asset at a specified location.
 *
 * @since 1.0.0
 */
@Data
@Entity
public class Asset {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, length = 1024)
    private String url;

    @Column(nullable = true, length = 1024)
    private String guid;

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

}
