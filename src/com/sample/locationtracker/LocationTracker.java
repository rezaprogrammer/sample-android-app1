package com.sample.locationtracker;

import com.sample.app1.R;
import com.sample.uiutils.UITextViewUpdater;

import android.app.Activity;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class LocationTracker extends Activity {
	
	protected LocationManager locMan;
	protected LocationPoler locPoler;
	TextView latitudeLabel;
	TextView longitudeLabel;
	
	public LocationTracker() { }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		locMan = (LocationManager) getSystemService(LOCATION_SERVICE);
		setContentView(R.layout.activity_location_tracker);
		latitudeLabel = (TextView) findViewById(R.id.latitude_reading);
		longitudeLabel = (TextView) findViewById(R.id.longitude_reading);

	}

	public Geocoder getGeoLocation() {
		return null;
	}

	public void stopTrackButtonOnClick(View view) {
		((Button) findViewById(R.id.track_button)).setEnabled(true);
		((Button) findViewById(R.id.stop_track_button)).setEnabled(false);
		if(locPoler != null)
			locPoler.stopPoling();
		locPoler = null;
	}
	
	public void trackButtonOnClick(View view) {
		// Track button has been clicked
		((Button) findViewById(R.id.track_button)).setEnabled(false);
		((Button) findViewById(R.id.stop_track_button)).setEnabled(true);

		locPoler = new LocationPoler(locMan) {
			@Override
			protected void locationUpdated(double lat, double lon) {
				runOnUiThread(new UITextViewUpdater(latitudeLabel, String.valueOf(lat)));
				runOnUiThread(new UITextViewUpdater(longitudeLabel, String.valueOf(lon)));
			}
		};
		
		locPoler.registerAsLocationListener();
		Thread t = new Thread(locPoler);
		t.start();
	}
}
