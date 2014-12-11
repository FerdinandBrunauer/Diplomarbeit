package digitalsalzburg.localstorage;

import java.io.File;
import java.io.IOException;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

public class KmzReader {

	public static File getKmlFile(File kmzFile) throws IOException {
		try {
			String tempFolderPath = System.currentTimeMillis() + "";
			ZipFile zipFile = new ZipFile(kmzFile.getAbsolutePath());
			final File tempFolder = new File(tempFolderPath);
			if (tempFolder.exists())
				tempFolder.delete();

			zipFile.extractAll(tempFolderPath);

			File[] tempFolderFiles = tempFolder.listFiles();
			 tempFolder.deleteOnExit();
//			Runtime.getRuntime().addShutdownHook(new Thread() {
//				@Override
//				public void run() {
//					tempFolder.delete();
//				}
//			});

			String kmlFilePath = null;

			for (File file : tempFolderFiles) {
				String extension = file.getAbsolutePath()
						.substring(file.getAbsolutePath().lastIndexOf('.') + 1)
						.toLowerCase();
				if (extension.compareTo("kml") == 0) {
					kmlFilePath = file.getAbsolutePath();
					break;
				} else {
					file.delete();
				}
			}

			if (kmlFilePath != null) {
				return new File(kmlFilePath);
			} else {
				throw new NullPointerException("No kml-file found!");
			}
		} catch (ZipException e) {
			throw new IOException("Error reading file.");
		}
	}

}
