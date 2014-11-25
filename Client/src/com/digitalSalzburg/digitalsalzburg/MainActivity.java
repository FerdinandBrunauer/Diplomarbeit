package com.digitalSalzburg.digitalsalzburg;

import android.app.Activity;
import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	    TextView tv = new TextView(this);
	    
		LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		LocationListener mlocListener = new LocationManagerHelper();
		
		mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
		        mlocListener);
		
		if (mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
		        tv.append("Latitude:- " + LocationManagerHelper.getLatitude()
		                + '\n');
		        tv.append("Longitude:- " + LocationManagerHelper.getLongitude()
		                + '\n');
		} else {
		    tv.setText("GPS is not turned on...");
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
