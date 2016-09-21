package tv.helixware.mico.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * @since 4.2.0
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CheckStatusResponse {

    private boolean finished;

    private String uri;

    private String time;

    private List<ContentPartResponse> parts;

}
