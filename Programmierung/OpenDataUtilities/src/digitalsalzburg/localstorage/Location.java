package digitalsalzburg.localstorage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Location {

	private double latitude;
	private double longitude;

	public Location(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public Location(String stringBetween) {
		Pattern locationPattern = Pattern.compile("\\<coordinates\\>[\\s](\\d+\\.\\d+)\\,(\\d+\\.\\d+)\\,\\d\\<\\/coordinates\\>", Pattern.CASE_INSENSITIVE);
		Matcher locationMatcher = locationPattern.matcher(stringBetween);
		while (locationMatcher.find()) {
			this.latitude = Double.parseDouble(locationMatcher.group(1));
			this.longitude = Double.parseDouble(locationMatcher.group(2));
		}
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	@Override
	public String toString() {
		return "Latitude: " + roundDouble(this.latitude, 2) + ", Longitude: " + roundDouble(this.longitude, 2);
	}

	private String roundDouble(double value, int countDigits) {
		double rounder = Double.parseDouble("1" + "000000000000000000000".substring(0, countDigits));
		return String.valueOf(Math.round(value * rounder) / rounder);
	}

}
