package at.htlhallein.gps;

public class GPSUtils {
	private final static double earthRadius = 6371; // KM

	// http://stackoverflow.com/questions/837872/calculate-distance-in-meters-when-you-know-longitude-and-latitude-in-java
	public static double distanceBetweenPoints(double latitude1, double longtitude1, double latitude2, double longtitude2) {
		double dLat = Math.toRadians(latitude2 - latitude1);
		double dLng = Math.toRadians(longtitude2 - longtitude1);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(latitude1)) * Math.cos(Math.toRadians(latitude2)) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double dist = (double) (earthRadius * c);

		return dist;
	}
}
