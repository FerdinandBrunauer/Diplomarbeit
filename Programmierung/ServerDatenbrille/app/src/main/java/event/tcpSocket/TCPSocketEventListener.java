package event.tcpSocket;

import java.util.EventListener;

public interface TCPSocketEventListener extends EventListener {
    public void TCPSocketEventOccurred(TCPSocketEventObject eventObject);
}
