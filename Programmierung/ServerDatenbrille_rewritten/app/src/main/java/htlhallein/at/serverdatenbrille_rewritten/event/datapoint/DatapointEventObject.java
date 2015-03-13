package htlhallein.at.serverdatenbrille_rewritten.event.datapoint;

import java.util.EventObject;

public class DatapointEventObject extends EventObject {
    private String htmlText;

    public DatapointEventObject(Object source, String htmlText) {
        super(source);
        this.htmlText = htmlText;
    }

    public String getHtmlText() {
        return this.htmlText;
    }
}
