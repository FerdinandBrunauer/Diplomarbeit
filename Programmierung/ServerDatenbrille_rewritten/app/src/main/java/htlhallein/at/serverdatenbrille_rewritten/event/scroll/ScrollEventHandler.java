package htlhallein.at.serverdatenbrille_rewritten.event.scroll;

import java.util.ArrayList;

public class ScrollEventHandler {
    private static ArrayList<ScrollEventListener> eventListener = new ArrayList<>();

    public static void addListener(ScrollEventListener eventListener1) {
        eventListener.add(eventListener1);
    }

    public static void removeListener(ScrollEventListener eventListener1) {
        eventListener.remove(eventListener1);
    }

    public static void fireScrollEvent(ScrollEventObject eventObject) {
        for (ScrollEventListener listener : eventListener) {
            listener.scrollEventOccurred(eventObject);
        }
    }
}
