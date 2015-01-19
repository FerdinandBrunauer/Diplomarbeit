package server.tcpService;

public class TcpServer {
    // TODO REWRITE/CHANGE TO NEEDS

//	public final static String PORT_PROP = "port";
//	private final static int PORT_DEFAULT = 1234;
//	private int port = PORT_DEFAULT;
//
//	public final static String EXECUTOR_PROP = "executor";
//	private final static Executor EXECUTOR_DEFAULT = Executors.newCachedThreadPool();
//	private Executor executor = EXECUTOR_DEFAULT;
//
//	public static enum State {
//		STARTING, STARTED, STOPPING, STOPPED
//	};
//
//	private State currentState = State.STOPPED;
//	public final static String STATE_PROP = "state";
//
//	private Collection<Listener> listeners = new LinkedList<Listener>(); // Event
//	// listeners
//	private Event event = new Event(this); // Shared event
//	private PropertyChangeSupport propSupport = new PropertyChangeSupport(this); // Properties
//
//	@SuppressWarnings("unused")
//	private TcpServer This = this; // To aid in synchronizing
//	private ThreadFactory threadFactory; // Optional thread factory
//	private Thread ioThread; // Performs IO
//	private ServerSocket tcpServer; // The server
//	private Socket socket;
//
//	public final static String LAST_EXCEPTION_PROP = "lastException";
//	private Throwable lastException;
//
//	public TcpServer() {
//	}
//
//	public TcpServer(int port) {
//		this.port = port;
//	}
//
//	public TcpServer(int port, ThreadFactory factory) {
//		this.port = port;
//		this.threadFactory = factory;
//	}
//
//	public synchronized void start() {
//		if (this.currentState == State.STOPPED) { // Only if we're stopped now
//			assert this.ioThread == null : this.ioThread; // Shouldn't have a
//			// thread
//
//			Runnable run = new Runnable() {
//				@Override
//				public void run() {
//					TcpServer.this.runServer(); // This runs for a long time
//					TcpServer.this.ioThread = null;
//					TcpServer.this.setState(State.STOPPED); // Clear thread
//				} // end run
//			}; // end runnable
//
//			if (this.threadFactory != null) { // User-specified threads
//				this.ioThread = this.threadFactory.newThread(run);
//
//			} else { // Our own threads
//				this.ioThread = new Thread(run, this.getClass().getName()); // Named
//			}
//
//			this.setState(State.STARTING); // Update state
//			this.ioThread.start(); // Start thread
//		} // end if: currently stopped
//	} // end start
//
//	public synchronized void stop() {
//		if (this.currentState == State.STARTED) { // Only if already STARTED
//			this.setState(State.STOPPING); // Mark as STOPPING
//			if (this.tcpServer != null) { //
//				try {
//					this.tcpServer.close();
//				} catch (IOException exc) {
//					Log.warn("An error occurred while closing the TCP server. " + "This may have left the server in an undefined state.", exc);
//					this.fireExceptionNotification(exc);
//				}
//			} // end if: not null
//		} // end if: already STARTED
//	} // end stop
//
//	public synchronized State getState() {
//		return this.currentState;
//	}
//
//	protected synchronized void setState(State state) {
//		State oldVal = this.currentState;
//		this.currentState = state;
//		this.firePropertyChange(STATE_PROP, oldVal, state);
//	}
//
//	@SuppressWarnings("incomplete-switch")
//	public synchronized void reset() {
//		switch (this.currentState) {
//		case STARTED:
//			this.addPropertyChangeListener(STATE_PROP, new PropertyChangeListener() {
//				@Override
//				public void propertyChange(PropertyChangeEvent evt) {
//					State newState = (State) evt.getNewValue();
//					if (newState == State.STOPPED) {
//						TcpServer server = (TcpServer) evt.getSource();
//						server.removePropertyChangeListener(STATE_PROP, this);
//						server.start();
//					} // end if: stopped
//				} // end prop change
//			});
//			this.stop();
//			break;
//		} // end switch
//	}
//
//	protected void runServer() {
//		try {
//			this.tcpServer = new ServerSocket(this.getPort()); // Create server
//			this.setState(State.STARTED); // Mark as started
//
//			while (!this.tcpServer.isClosed()) {
//				synchronized (this) {
//					if (this.currentState == State.STOPPING) {
//						this.tcpServer.close();
//					} // end if: stopping
//				} // end sync
//
//				if (!this.tcpServer.isClosed()) {
//
//					// ////// B L O C K I N G
//					this.socket = this.tcpServer.accept();
//					// ////// B L O C K I N G
//
//					this.fireTcpServerSocketReceived();
//
//				} // end if: not closed
//			} // end while: keepGoing
//
//		} catch (Exception exc) {
//			synchronized (this) {
//				if (this.currentState == State.STOPPING) { // User asked to stop
//					try {
//						this.tcpServer.close();
//					} catch (IOException exc2) {
//						Log.warn("An error occurred while closing the TCP server. " + "This may have left the server in an undefined state.", exc2);
//						this.fireExceptionNotification(exc2);
//					} // end catch IOException
//				} else {
//					Log.warn("Server closed unexpectedly: " + exc.getMessage(), exc);
//				} // end else
//			} // end sync
//			this.fireExceptionNotification(exc);
//		} finally {
//			this.setState(State.STOPPING);
//			if (this.tcpServer != null) {
//				try {
//					this.tcpServer.close();
//				} catch (IOException exc2) {
//					Log.warn("An error occurred while closing the TCP server. " + "This may have left the server in an undefined state.", exc2);
//					this.fireExceptionNotification(exc2);
//				} // end catch IOException
//			} // end if: not null
//			this.tcpServer = null;
//		}
//	}
//
//	/* ******** S O C K E T ******** */
//
//	public synchronized Socket getSocket() {
//		return this.socket;
//	}
//
//	/* ******** P O R T ******** */
//
//	public synchronized int getPort() {
//		return this.port;
//	}
//
//	public synchronized void setPort(int port) {
//		if ((port < 0) || (port > 65535)) {
//			throw new IllegalArgumentException("Cannot set port outside range 0..65535: " + port);
//		} // end if: port outside range
//
//		int oldVal = this.port;
//		this.port = port;
//		if ((this.getState() == State.STARTED) && (oldVal != port)) {
//			this.reset();
//		} // end if: is running
//
//		this.firePropertyChange(PORT_PROP, oldVal, port);
//	}
//
//	/* ******** E X E C U T O R ******** */
//
//	public synchronized Executor getExecutor() {
//		return this.executor;
//	}
//
//	public synchronized void setExecutor(Executor exec) {
//		Executor oldVal = this.executor;
//		this.executor = exec;
//
//		this.firePropertyChange(EXECUTOR_PROP, oldVal, exec);
//	}
//
//	public synchronized void addTcpServerListener(Listener l) {
//		this.listeners.add(l);
//	}
//
//	public synchronized void removeTcpServerListener(Listener l) {
//		this.listeners.remove(l);
//	}
//
//	protected synchronized void fireTcpServerSocketReceived() {
//
//		final Listener[] ll = this.listeners.toArray(new Listener[this.listeners.size()]);
//
//		// Make a Runnable object to execute the calls to listeners.
//		// In the event we don't have an Executor, this results in
//		// an unnecessary object instantiation, but it also makes
//		// the code more maintainable.
//		Runnable r = new Runnable() {
//			@Override
//			public void run() {
//				for (Listener l : ll) {
//					try {
//						l.socketReceived(TcpServer.this.event);
//					} catch (Exception exc) {
//						Log.warn("TcpServer.Listener " + l + " threw an exception: " + exc.getMessage());
//						TcpServer.this.fireExceptionNotification(exc);
//					} // end catch
//				} // end for: each listener
//			} // end run
//		};
//
//		if (this.executor == null) {
//			r.run();
//		} else {
//			try {
//				this.executor.execute(r);
//			} catch (Exception exc) {
//				Log.warn("Supplied Executor " + this.executor + " threw an exception: " + exc.getMessage());
//				this.fireExceptionNotification(exc);
//			} // end catch
//		} // end else: other thread
//	} // end fireTcpServerPacketReceived
//
//	/* ******** P R O P E R T Y C H A N G E ******** */
//
//	public synchronized void fireProperties() {
//		this.firePropertyChange(PORT_PROP, null, this.getPort()); // Port
//		this.firePropertyChange(STATE_PROP, null, this.getState()); // State
//	}
//
//	protected synchronized void firePropertyChange(final String prop, final Object oldVal, final Object newVal) {
//		try {
//			this.propSupport.firePropertyChange(prop, oldVal, newVal);
//		} catch (Exception exc) {
//			Log.warn("A property change listener threw an exception: " + exc.getMessage(), exc);
//			this.fireExceptionNotification(exc);
//		} // end catch
//	} // end fire
//
//	public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
//		this.propSupport.addPropertyChangeListener(listener);
//	}
//
//	public synchronized void addPropertyChangeListener(String property, PropertyChangeListener listener) {
//		this.propSupport.addPropertyChangeListener(property, listener);
//	}
//
//	public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
//		this.propSupport.removePropertyChangeListener(listener);
//	}
//
//	public synchronized void removePropertyChangeListener(String property, PropertyChangeListener listener) {
//		this.propSupport.removePropertyChangeListener(property, listener);
//	}
//
//	/* ******** E X C E P T I O N S ******** */
//
//	public synchronized Throwable getLastException() {
//		return this.lastException;
//	}
//
//	protected void fireExceptionNotification(Throwable t) {
//		Throwable oldVal = this.lastException;
//		this.lastException = t;
//		this.firePropertyChange(LAST_EXCEPTION_PROP, oldVal, t);
//	}

}
