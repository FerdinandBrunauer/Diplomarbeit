package htlhallein.at.serverdatenbrille.event.datapoint;

import java.util.EventObject;

public class DatapointEventObject extends EventObject {
    private String htmlText;

    public DatapointEventObject(String htmlText) {
        super("");
        this.htmlText = htmlText;
    }

    public String getHtmlText() {
        return this.htmlText;
    }
}
