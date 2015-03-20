package htlhallein.at.serverdatenbrille.event.datapoint;

public interface DatapointEventListener {
    public abstract void datapointEventOccurred(DatapointEventObject eventObject);
}
