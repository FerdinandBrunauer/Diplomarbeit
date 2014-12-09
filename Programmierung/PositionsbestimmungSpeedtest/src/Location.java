public class Location {
	final private static double hundred = 100.0d;

	private double latitude;
	private double longitude;

	public Location(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	@Override
	public String toString() {
		return String.valueOf(Math.round(this.latitude * hundred)/hundred) + " " + String.valueOf(Math.round(this.longitude * hundred)/hundred);
	}

}
