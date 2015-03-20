package htlhallein.at.serverdatenbrille.memoryObjects;

import htlhallein.at.serverdatenbrille.opendata.OpenDataUtil;

public class OpenDataResource {
    private String id;
    private String format;
    private String url;
    private long creationTimestamp;

    public OpenDataResource(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(long revisionTimestamp) {
        this.creationTimestamp = revisionTimestamp;
    }

    public boolean checkForUpdate() {
        OpenDataResource res = OpenDataUtil.getResourceById(id);

        if (res.getCreationTimestamp() > creationTimestamp) {
            return true;
        }
        return false;
    }


}
