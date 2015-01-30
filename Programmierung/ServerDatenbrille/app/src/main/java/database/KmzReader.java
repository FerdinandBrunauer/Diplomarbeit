/*
 * Copyright 2015 [Alexander Bendl, Brunauer Ferdinand, Milena Matic]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package database;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.IOException;

public class KmzReader {

	public static File getKmlFile(String kmzFileName) throws IOException {

		try {
			String tempFolderPath = Environment.getExternalStorageDirectory() + "/datenbrille/temp/TEMP_" + System.currentTimeMillis() + "/";
            String zipFile = Environment.getExternalStorageDirectory() + "/datenbrille/download/" + kmzFileName;

            Decompress d = new Decompress(zipFile, tempFolderPath);
            d.unzip();

			final File tempFolder = new File(tempFolderPath);
            //if (tempFolder.exists())
			//	tempFolder.delete();



			File[] tempFolderFiles = tempFolder.listFiles();
			// tempFolder.deleteOnExit();

			String kmlFilePath = null;

			for (int i=0;i< tempFolderFiles.length;i++) {
				String extension = tempFolderFiles[i].getAbsolutePath().substring(tempFolderFiles[i].getAbsolutePath().lastIndexOf('.') + 1).toLowerCase();
				if (extension.compareTo("kml") == 0) {
					kmlFilePath = tempFolderFiles[i].getAbsolutePath();
					break;
				} else {
                    tempFolderFiles[i].delete();
				}
			}

			if (kmlFilePath != null) {
				File file = new File(kmlFilePath);
				tempFolder.deleteOnExit();
				file.deleteOnExit();
				return file;
			} else {
				throw new NullPointerException("No kml-file found!");
			}
		} catch (Exception e) {
			throw new IOException("Error reading file.");
		}
	}

}
