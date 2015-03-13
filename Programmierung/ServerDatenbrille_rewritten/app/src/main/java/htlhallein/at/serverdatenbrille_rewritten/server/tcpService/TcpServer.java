package htlhallein.at.serverdatenbrille_rewritten.server.tcpService;

import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import htlhallein.at.serverdatenbrille_rewritten.event.tcpSocket.TCPSocketEventHandler;
import htlhallein.at.serverdatenbrille_rewritten.event.tcpSocket.TCPSocketEventObject;

public class TcpServer {
    public final static int PORT = 6484;

    private TcpServerState currentState = TcpServerState.STOPPED;

    @SuppressWarnings("unused")
    private TcpServer This = this; // to make it ThreadSafe
    private Thread ioThread;
    private ServerSocket tcpServer;

    public synchronized void start() {
        if (this.currentState == TcpServerState.STOPPED) {
            assert this.ioThread == null : this.ioThread;
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    TcpServer.this.runServer();
                    TcpServer.this.ioThread = null;
                    TcpServer.this.currentState = TcpServerState.STOPPED;
                }
            };

            this.ioThread = new Thread(run, this.getClass().getName());

            this.currentState = TcpServerState.STARTING;
            this.ioThread.start();
        }
    }

    public synchronized void stop() {
        if (this.currentState == TcpServerState.STARTED) {
            this.currentState = TcpServerState.STOPPING;
            if (this.tcpServer != null) {
                try {
                    this.tcpServer.close();
                } catch (IOException exc) {
                    System.err.println("An error occurred while closing the TCP server. " + "This may have left the server in an undefined state. " + exc.getMessage());
                }
            }
        }
    }

    protected void runServer() {
        Log.v("TcpServer", "Server started ...");

        try {
            this.tcpServer = new ServerSocket(TcpServer.PORT);
            Log.v("TcpServer", "ServerSocket created ...");
            this.currentState = TcpServerState.STARTED;

            while (!this.tcpServer.isClosed()) {
                synchronized (this) {
                    if (this.currentState == TcpServerState.STOPPING)
                        this.tcpServer.close();
                }

                if (!this.tcpServer.isClosed()) {
                    //////// B L O C K I N G
                    Log.v("TcpServer", "Waiting for new Client ...");
                    Socket newClient = this.tcpServer.accept();
                    Log.v("TcpServer", "new Client recieved ...");
                    // ////// B L O C K I N G
                    TCPSocketEventObject eventObject = new TCPSocketEventObject(this, newClient);
                    TCPSocketEventHandler.fireTCPSocketEvent(eventObject);
                }
            }

        } catch (Exception exc) {
            synchronized (this) {
                if (this.currentState == TcpServerState.STOPPING) {
                    try {
                        this.tcpServer.close();
                    } catch (IOException exc2) {
                        System.err.println("An error occurred while closing the TCP server. " + "This may have left the server in an undefined state. " + exc2.getMessage());
                    }
                } else {
                    System.err.println("Server closed unexpectedly: " + exc.getMessage() + exc.getMessage());
                }
            }
        } finally {
            this.currentState = TcpServerState.STOPPING;
            if (this.tcpServer != null) {
                try {
                    this.tcpServer.close();
                } catch (IOException exc2) {
                    System.err.println("An error occurred while closing the TCP server. " + "This may have left the server in an undefined state. " + exc2.getMessage());
                }
            }
            this.tcpServer = null;
        }
    }

    public TcpServerState getState() {
        return this.currentState;
    }
}
