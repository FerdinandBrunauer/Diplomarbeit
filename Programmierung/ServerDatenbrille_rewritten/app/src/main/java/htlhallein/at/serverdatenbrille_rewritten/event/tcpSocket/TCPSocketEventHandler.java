package htlhallein.at.serverdatenbrille_rewritten.event.tcpSocket;

import java.util.ArrayList;

public class TCPSocketEventHandler {
    private static ArrayList<TCPSocketEventListener> eventListener = new ArrayList<>();

    public static void addListener(TCPSocketEventListener eventListener1) {
        eventListener.add(eventListener1);
    }

    public static void removeListener(TCPSocketEventListener eventListener1) {
        eventListener.remove(eventListener1);
    }

    public static void fireTCPSocketEvent(TCPSocketEventObject eventObject) {
        for (TCPSocketEventListener listener : eventListener) {
            listener.TCPSocketEventOccurred(eventObject);
        }
    }
}
