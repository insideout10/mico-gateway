package tv.helixware.mico.helpers;

import com.github.anno4j.model.Annotation;
import com.github.anno4j.model.Target;

/**
 * A helper class that ease migration from Anno4j 1.1 to 2.3.
 *
 * @since 0.2.0
 */
public class AnnotationHelper {

    public static Target target(final Annotation annotation) {

        return annotation.getTarget().stream().findFirst().get();
    }

}
