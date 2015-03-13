package htlhallein.at.serverdatenbrille_rewritten.event.tcpSocket;

import java.util.EventListener;

public interface TCPSocketEventListener extends EventListener {
    public void TCPSocketEventOccurred(TCPSocketEventObject eventObject);
}
