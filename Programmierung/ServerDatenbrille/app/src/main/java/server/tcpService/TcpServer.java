package server.tcpService;

import java.net.ServerSocket;
import java.net.Socket;

import event.tcpSocket.TCPSocketEventHandler;
import event.tcpSocket.TCPSocketEventObject;

public class TCPServer {

    public final static int PORT = 4567;

    private Thread myIOThread;
    private ServerSocket myTcpServer;

    private State myState = State.STOPPED;

    public synchronized void start() {
        if (myState == State.STOPPED) {
            assert myIOThread == null : myIOThread;
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    runServer();
                    myIOThread = null;
                    myState = State.STOPPED;
                }
            };

            myIOThread = new Thread(run, this.getClass().getName());

            this.myState = State.STARTING;
            myIOThread.start();
        }
    }

    public synchronized void stop() {
        if (myState == State.STARTED) {
            myState = State.STOPPING;
            if (myTcpServer != null) {
                try {
                    this.myTcpServer.close();
                } catch (Exception e) {
                    // I can not do anything against that ...
                }
            }
        }
    }

    protected void runServer() {
        try {
            myTcpServer = new ServerSocket(PORT);
            myState = State.STARTED;

            while (myTcpServer.isClosed()) {
                synchronized (this) {
                    if (myState == State.STOPPING)
                        myTcpServer.close();
                }

                if (!myTcpServer.isClosed()) {
                    Socket client = myTcpServer.accept();
                    TCPSocketEventObject eventObject = new TCPSocketEventObject(this, client);
                    TCPSocketEventHandler.fireScrollEvent(eventObject);
                }
            }
        } catch (Exception e) {
            if (myState == State.STOPPING) {
                try {
                    myTcpServer.close();
                } catch (Exception e1) {
                    // I can not do anything against that ...
                }
            } // else -> closed unexpectically .. Shit happens
        } finally {
            if (myTcpServer != null) {
                try {
                    myTcpServer.close();
                } catch (Exception ex2) {
                    // Error while closing TCP - Server
                }
            }
        }
    }

    public synchronized State getMyState() {
        return myState;
    }
}
