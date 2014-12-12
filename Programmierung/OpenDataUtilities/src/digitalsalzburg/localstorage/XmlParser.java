package digitalsalzburg.localstorage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.sun.xml.internal.messaging.saaj.packaging.mime.internet.ParseException;

public class XmlParser {

	public static ArrayList<Placemark> getPlacemarksFromKmlFile(File kmlFile) throws ParseException, IOException {
		BufferedReader br = null;
		try {
			ArrayList<Placemark> placemarks = new ArrayList<Placemark>();
			// br = new BufferedReader(new FileReader(kmlFile));
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
			throw new ParseException("Error occured at parsing file.");
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				throw new IOException("Error: Can't read file.");
			}
		}
	}

}
