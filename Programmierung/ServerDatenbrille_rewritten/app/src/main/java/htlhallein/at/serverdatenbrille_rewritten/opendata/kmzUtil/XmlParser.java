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

package htlhallein.at.serverdatenbrille_rewritten.opendata.kmzUtil;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import htlhallein.at.serverdatenbrille_rewritten.memoryObjects.Placemark;

public class XmlParser {

	public static ArrayList<Placemark> getPlacemarksFromKmlFile(File kmlFile) {
		BufferedReader br = null;
		try {
			ArrayList<Placemark> placemarks = new ArrayList<Placemark>();
			br = new BufferedReader(new InputStreamReader(new FileInputStream(kmlFile), "UTF-8"));
			String line;
			StringBuilder currentPlacemarkBuilder = new StringBuilder();
			while ((line = br.readLine()) != null) {
				if (line.contains("<Placemark id=\"")) {
					currentPlacemarkBuilder = new StringBuilder();
					currentPlacemarkBuilder.append(line);
				} else if (line.contains("</Placemark>")) {
					placemarks.add(new Placemark(currentPlacemarkBuilder.toString()));
				} else {
					currentPlacemarkBuilder.append(line);
				}
			}
			return placemarks;
		} catch (Exception e) {
            Log.e("XmlParser.getPlacemarksFromKmlFile", "Parse Error: ", e);
            return null;
		} finally {
			try {
				br.close();
			} catch (IOException e) {
                Log.e("XmlParser.getPlacemarksFromKmlFile", "Can't read File: ", e);
                return null;
			}
		}
	}

}
