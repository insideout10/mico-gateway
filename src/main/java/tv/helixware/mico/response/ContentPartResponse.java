package tv.helixware.mico.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * @since 4.2.0
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContentPartResponse {

    private String title;
    private String source;
    private List<Object> transitions;
    private String created;
    private String state;
    private String type;
    private String uri;
    private String creator;

}
