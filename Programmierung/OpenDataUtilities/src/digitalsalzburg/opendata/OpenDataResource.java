package digitalsalzburg.opendata;

import java.util.Date;

public class OpenDataResource {
	private String id;
	private String format;
	private String url;
	private Date revisionTimestamp;
	
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

	public Date getRevisionTimestamp() {
		return revisionTimestamp;
	}

	public void setRevisionTimestamp(Date revisionTimestamp) {
		this.revisionTimestamp = revisionTimestamp;
	}
	
	
}
