package htlhallein.at.serverdatenbrille_rewritten.drawer;

public class NsMenuItemModel {

    public int id;
    public int title;
    public int iconRes;
    public int counter;
    public boolean isHeader;

    public NsMenuItemModel(int title, int iconRes, boolean header, int counter, int id) {
        this.title = title;
        this.iconRes = iconRes;
        this.isHeader = header;
        this.counter = counter;
        this.id = id;
    }

    public NsMenuItemModel(int title, int iconRes, boolean header, int id) {
        this(title, iconRes, header, 0, id);
    }

    public NsMenuItemModel(int title, int iconRes, int id) {
        this(title, iconRes, false, id);
    }


}
