package digitalsalzburg.localstorage;

import java.io.File;

public class Debug {

	public static void main(String[] args) {
		try {
			File kmlFile = KmzReader.getKmlFile(new File("Museen.kmz"));
			/*ArrayList<Placemark> placemarks = */XmlParser.getPlacemarksFromKmlFile(kmlFile);
		} catch (Exception e) {
			System.err.println(e);
		}
	}

}
