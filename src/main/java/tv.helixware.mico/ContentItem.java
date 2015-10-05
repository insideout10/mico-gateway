package tv.helixware.mico;

/**
 * @since 4.2.0
 */
public class ContentItem {

    private final String uri;
    private final String uuid;

    /**
     * Create an instance of ContentItem with the specified UUID.
     *
     * @param uuid The unique ID for this ContentItem.
     * @since 4.2.0
     */
    public ContentItem(final String uri, final String uuid) {

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
