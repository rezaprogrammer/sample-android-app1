package com.sample.locationtracker;

import com.sample.app1.R;
import com.sample.uiutils.UITextViewUpdater;

import android.app.Activity;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class LocationTracker extends Activity {
	
	protected LocationManager locMan;
	protected LocationPoler locPoler;
	
	public LocationTracker() { }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		locMan = (LocationManager) getSystemService(LOCATION_SERVICE);
		setContentView(R.layout.activity_location_tracker);
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

		locPoler = new LocationPoler(this);
		locPoler.registerAsLocationListener();
		
		Thread t = new Thread(locPoler);
		t.start();
	}
	
	class LocationPoler implements Runnable, LocationListener {

		private LocationManager locMan;
		private LocationTracker locTracker;
		private final String PREFERRED_LOC_PROVIDER = LocationManager.GPS_PROVIDER;
		private final int POLLING_INTERVAL_MS = 1 * 1000;
		private boolean continuePole = true;
		private double lat = 0;
		private double lon = 0;
		
		public LocationPoler(LocationTracker locTracker) {
			this.locTracker = locTracker;
			this.locMan = locTracker.locMan;
		}
		
		void registerAsLocationListener() {
			locMan.requestLocationUpdates(PREFERRED_LOC_PROVIDER, POLLING_INTERVAL_MS, 10, locPoler);
		}

		void stopPoling() {
			continuePole = false;	
		}

		public boolean continuePolling() {
			boolean ret = continuePole;
			ret &= this.locMan.isProviderEnabled(PREFERRED_LOC_PROVIDER);
			return ret;
		}
		
		@Override
		public void run() {
			TextView latitudeLabel = (TextView) locTracker.findViewById(R.id.latitude_reading);
			TextView longitudeLabel = (TextView) locTracker.findViewById(R.id.longitude_reading);

			while(continuePolling()) {
				try {
					runOnUiThread(new UITextViewUpdater(latitudeLabel, String.valueOf(lat)));
					runOnUiThread(new UITextViewUpdater(longitudeLabel, String.valueOf(lon)));
					
					Thread.sleep(POLLING_INTERVAL_MS);
				} catch (InterruptedException e) {
					e.printStackTrace();
					break;
				}
			}
		}

		@Override
		public void onLocationChanged(Location location) {
			lat = location.getLatitude();
			lon = location.getLongitude();
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			lat = lon = 0;
			return;
		}

		@Override
		public void onProviderEnabled(String provider) {
			if(PREFERRED_LOC_PROVIDER.compareToIgnoreCase(provider) == 0)
				this.locMan.requestLocationUpdates(PREFERRED_LOC_PROVIDER, POLLING_INTERVAL_MS, 10, this);
		}

		@Override
		public void onProviderDisabled(String provider) {
			this.continuePole = false;
			lat = lon = -1;
		}
	}
}
