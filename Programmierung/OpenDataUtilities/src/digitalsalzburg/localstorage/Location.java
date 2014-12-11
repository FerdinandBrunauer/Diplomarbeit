package digitalsalzburg.localstorage;


public class Location {

	private double latitude;
	private double longitude;
	
	public Location(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public Location(String stringBetween) {
		// Pattern locationPattern = Pattern.compile("");
		// TODO
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}
	
}
