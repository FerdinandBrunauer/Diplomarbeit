package database.openDataUtilities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import database.Location;

public class Datapoint {
    private int idDatapoint;
    private String openDataPackageID;
    private Location location;
    private String weblink;
    private String description;
    private String title;
    private String name;
    private Bitmap image;

    public Datapoint(int idDatapoint, String openDataPackageID, Location location, String weblink, String description, String title, String name, byte[] imageRaw) {
        this.idDatapoint = idDatapoint;
        this.openDataPackageID = openDataPackageID;
        this.location = location;
        this.weblink = weblink;
        this.description = description;
        this.title = title;
        this.name = name;
        if (imageRaw != null) {
            this.image = BitmapFactory.decodeByteArray(imageRaw, 0, imageRaw.length);
            imageRaw = null;
        } else {
            this.image = null;
        }
    }

    public int getIdDatapoint() {
        return idDatapoint;
    }

    public String getOpenDataPackageID() {
        return openDataPackageID;
    }

    public Location getLocation() {
        return location;
    }

    public String getWeblink() {
        return weblink;
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }

    public String getName() {
        return name;
    }

    public Bitmap getImage() {
        return image;
    }

}
