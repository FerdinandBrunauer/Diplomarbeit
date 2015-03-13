package htlhallein.at.serverdatenbrille_rewritten.opendata;

import java.util.ArrayList;
import java.util.List;

public class OpenDataPackage {
    private String id;
    private String name;
    private String notes;
    private List<OpenDataResource> resources;
    private List<OpenDataTag> tags;
    private String title;


    public OpenDataPackage(String id) {
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

    public void addResource(OpenDataResource resource) {
        this.resources.add(resource);
    }

    public List<OpenDataTag> getTags() {
        return tags;
    }

    public void setTags(List<OpenDataTag> tags) {
        this.tags = tags;
    }

    public void addTag(OpenDataTag tag) {
        this.tags.add(tag);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
