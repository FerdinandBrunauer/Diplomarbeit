package digitalsalzburg.localstorage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import com.sun.xml.internal.messaging.saaj.packaging.mime.internet.ParseException;

public class XmlParser {

	public static ArrayList<Placemark> getPlacemarksFromKmlFile(File kmlFile) throws ParseException {
		try {
			ArrayList<Placemark> placemarks = new ArrayList<Placemark>();
			BufferedReader br = new BufferedReader(new FileReader(kmlFile));
			String line;
			StringBuilder currentPlacemarkBuilder = new StringBuilder();
			while ((line = br.readLine()) != null) {
				if(line.contains("<Placemark id=\"")){
					currentPlacemarkBuilder = new StringBuilder();
				} else if(line.contains("</Placemark>")){
					placemarks.add(new Placemark(currentPlacemarkBuilder.toString()));
				} else {
					currentPlacemarkBuilder.append(line);
				}
			}
			br.close();
			return placemarks;
		} catch (Exception e) {
			throw new ParseException("Error occured at parsing file.");
		}
	}

}
