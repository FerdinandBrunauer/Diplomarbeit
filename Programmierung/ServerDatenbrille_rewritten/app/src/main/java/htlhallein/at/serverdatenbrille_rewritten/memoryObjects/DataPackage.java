package htlhallein.at.serverdatenbrille_rewritten.memoryObjects;

public class DataPackage {
    private long id;
    private String name;
    private String idOpenData;

    public DataPackage(long id, String name, String idOpenData) {
        this.id = id;
        this.name = name;
        this.idOpenData = idOpenData;
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
}