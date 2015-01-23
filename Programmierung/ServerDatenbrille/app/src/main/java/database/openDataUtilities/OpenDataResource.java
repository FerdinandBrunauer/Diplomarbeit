/*
 * Copyright 2015 [Alexander Bendl, Brunauer Ferdinand, Milena Matic]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package database.openDataUtilities;

public class OpenDataResource {
	private String id;
	private String format;
	private String url;
	private long creationTimestamp;
	
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

	public long getCreationTimestamp() {
		return creationTimestamp;
	}

	public void setCreationTimestamp(long revisionTimestamp) {
		this.creationTimestamp = revisionTimestamp;
	}
	
	public boolean checkForUpdate(){
		OpenDataResource res = OpenDataUtilities.getResourceById(id);
		
		if(res.getCreationTimestamp() > creationTimestamp){
			return true;
		}
		return false;
	}
	
	
}
