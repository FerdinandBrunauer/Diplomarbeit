package htlhallein.at.serverdatenbrille_rewritten.event.datapoint;

public interface DatapointEventListener {
    public abstract void datapointEventOccurred(DatapointEventObject eventObject);
}
