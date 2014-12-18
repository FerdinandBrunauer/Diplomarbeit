package database;

import java.io.File;

import com.almworks.sqlite4java.SQLiteQueue;

public class DatabaseConnection {

	private static SQLiteQueue queue = null;

	private static SQLiteQueue getQueue() {
		if (queue == null) {
			queue = new SQLiteQueue(new File("database.d3b"));
			queue.start();

			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					queue.stop(true);
				}
			});
		}
		return queue;
	}

	private static void createTables() {
		
	}

}
