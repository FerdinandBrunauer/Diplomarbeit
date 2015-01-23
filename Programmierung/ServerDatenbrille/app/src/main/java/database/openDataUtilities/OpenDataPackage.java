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

import java.util.ArrayList;
import java.util.List;

public class OpenDataPackage {
	private String id;
	private String name;
	private String notes;
	private List<OpenDataResource> resources;
	private List<OpenDataTag> tags;
	private String title;
	
	
	public OpenDataPackage(String id){
		resources = new ArrayList<OpenDataResource>();
		tags = new ArrayList<OpenDataTag>();
		
		this.id = id;
	}


	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getNotes() {
		return notes;
	}


	public void setNotes(String notes) {
		this.notes = notes;
	}


	public List<OpenDataResource> getResources() {
		return resources;
	}


	public void setResources(List<OpenDataResource> resources) {
		this.resources = resources;
	}
	
	public void addResource(OpenDataResource resource){
		this.resources.add(resource);
	}


	public List<OpenDataTag> getTags() {
		return tags;
	}


	public void setTags(List<OpenDataTag> tags) {
		this.tags = tags;
	}
	
	public void addTag(OpenDataTag tag){
		this.tags.add(tag);
	}


	public String getTitle() {
		return title;
	}


	public void setTitle(String title) {
		this.title = title;
	}
	
	
}
