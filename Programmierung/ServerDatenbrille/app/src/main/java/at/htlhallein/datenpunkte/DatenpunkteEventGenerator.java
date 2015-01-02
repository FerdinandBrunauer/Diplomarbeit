package at.htlhallein.datenpunkte;

import java.util.ArrayList;

/**
 * Created by Toshiba on 02.01.2015.
 */
public class DatenpunkteEventGenerator {
    private ArrayList<DatenpunkteListener> listeners = new ArrayList<DatenpunkteListener>();

    public synchronized void fireDatapointEvent(DatenpunkteEventObject eventObject) {
        for (DatenpunkteListener listener : this.listeners) {
            listener.newDatapoint(eventObject);
        }
    }

    public synchronized void addMoodListener(DatenpunkteListener l) {
        listeners.add(l);
    }

    public synchronized void removeMoodListener(DatenpunkteListener l) {
        listeners.remove(l);
    }
}
