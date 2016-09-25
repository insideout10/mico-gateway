package tv.helixware.mico.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;

/**
 * Represents an asset at a specified location.
 *
 * @since 0.1.0
 */
@Data
@Entity
// Generated constructors
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
// JSON configuration: ignore the following properties when creating a new class instance from JSON payloads.
@JsonIgnoreProperties({"id", "version", "createdDate", "lastModifiedDate"})
public class Asset {

    @Id
    @GeneratedValue
    // We don't accept IDs from remote requests.
//    @JsonIgnore
//    @Setter(onMethod = @__(@JsonIgnore))
    private Long id;

    @NonNull
    @Column(nullable = false, length = 1024)
    private String url;

    @NonNull
    @Column(length = 1024)
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
