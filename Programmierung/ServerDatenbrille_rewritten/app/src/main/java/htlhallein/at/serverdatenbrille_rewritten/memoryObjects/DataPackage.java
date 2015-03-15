package htlhallein.at.serverdatenbrille_rewritten.memoryObjects;

public class DataPackage {
    private long id;
    private String name;
    private String idOpenData;
    private boolean datapointsInstalled;
    private long updated;

    public DataPackage(long id, String name, String idOpenData, boolean datapointsInstalled, long updated) {
        this.id = id;
        this.name = name;
        this.idOpenData = idOpenData;
        this.datapointsInstalled = datapointsInstalled;
        this.updated = updated;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdOpenData() {
        return idOpenData;
    }

    public void setIdOpenData(String idOpenData) {
        this.idOpenData = idOpenData;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isDatapointsInstalled() {
        return datapointsInstalled;
    }

    public void setDatapointsInstalled(boolean datapointsInstalled) {
        this.datapointsInstalled = datapointsInstalled;
    }
}