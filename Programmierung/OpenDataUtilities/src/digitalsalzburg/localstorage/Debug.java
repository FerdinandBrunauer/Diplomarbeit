package digitalsalzburg.localstorage;

import java.io.File;
import java.util.ArrayList;

public class Debug {

	public static void main(String[] args) {
		try {
			File kmlFile = KmzReader.getKmlFile(new File("Museen.kmz"));
			ArrayList<Placemark> placemarks = XmlParser.getPlacemarksFromKmlFile(kmlFile);
			for (Placemark placemark : placemarks) {
				placemark.print();
			}
		} catch (Exception e) {
			System.err.println(e);
		}
	}

}
