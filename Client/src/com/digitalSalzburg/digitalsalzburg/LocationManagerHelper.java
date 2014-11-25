package com.digitalSalzburg.digitalsalzburg;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

public class LocationManagerHelper implements LocationListener{

    private static double latitude;
    private static double longitude;

	@Override
	public void onLocationChanged(Location loc) {
		latitude = loc.getLatitude();
        longitude = loc.getLongitude();
	}

	@Override
	public void onProviderDisabled(String arg0) {
		
		
	}

	@Override
	public void onProviderEnabled(String arg0) {
		
		
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		
		
	}
	
	public static double getLongitude(){
		return longitude;
	}
	public static double getLatitude(){
		return latitude;
	}
}
