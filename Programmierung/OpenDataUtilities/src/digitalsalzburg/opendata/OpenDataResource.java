package digitalsalzburg.opendata;

public class OpenDataResource {
	private String id;
	private String format;
	private String url;
	private long revisionTimestamp;
	
	public OpenDataResource(String id){
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

	public long getRevisionTimestamp() {
		return revisionTimestamp;
	}

	public void setRevisionTimestamp(long revisionTimestamp) {
		this.revisionTimestamp = revisionTimestamp;
	}
	
	
}
