package htlhallein.at.serverdatenbrille.event.scroll;

import java.util.EventListener;

public interface ScrollEventListener extends EventListener {
    public void scrollEventOccurred(ScrollEventObject eventObject);
}
