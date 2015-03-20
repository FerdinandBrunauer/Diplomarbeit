package htlhallein.at.serverdatenbrille.event.scroll;

import java.util.ArrayList;

public class ScrollEventHandler {
    private static ArrayList<ScrollEventListener> eventListener = new ArrayList<>();

    public static synchronized void addListener(ScrollEventListener eventListener1) {
        eventListener.add(eventListener1);
    }

    public static synchronized void removeListener(ScrollEventListener eventListener1) {
        eventListener.remove(eventListener1);
    }

    public static synchronized void fireScrollEvent(ScrollEventObject eventObject) {
        for (ScrollEventListener listener : eventListener) {
            listener.scrollEventOccurred(eventObject);
        }
    }

    public static void clearListener() {
        eventListener.clear();
    }
}
