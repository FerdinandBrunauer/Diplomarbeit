package at.htlhallein.datenpunkte;

import java.util.EventObject;

/**
 * Created by Toshiba on 02.01.2015.
 */
public class DatenpunkteEvent extends EventObject {
    private DatenpunkteEventObject eventObject;

    public DatenpunkteEvent(Object source, DatenpunkteEventObject eventObject) {
        super(source);
        this.eventObject = eventObject;
    }
}
