package htlhallein.at.serverdatenbrille_rewritten.event.tcpSocket;

import java.net.Socket;
import java.util.EventObject;

public class TCPSocketEventObject extends EventObject {

    private Socket socket;

    public TCPSocketEventObject(Object source, Socket socket) {
        super(source);
        this.socket = socket;
    }

    public Socket getSocket() {
        return socket;
    }

}
