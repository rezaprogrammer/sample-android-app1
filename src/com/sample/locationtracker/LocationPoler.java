package com.sample.locationtracker;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public abstract class LocationPoler implements Runnable, LocationListener {

	protected LocationManager locMan;
	protected final String PREFERRED_LOC_PROVIDER = LocationManager.GPS_PROVIDER;
	protected final int POLLING_INTERVAL_MS = 1 * 1000;
	private boolean continuePole = true;
	protected double lat = 0;
	protected double lon = 0;

	
	public LocationPoler(LocationManager locMan) {
		this.locMan = locMan;
	}
	
	public void registerAsLocationListener() {
		locMan.requestLocationUpdates(PREFERRED_LOC_PROVIDER, POLLING_INTERVAL_MS, 10, this);
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
		Location loc = locMan.getLastKnownLocation(PREFERRED_LOC_PROVIDER);
		lat = loc.getLatitude();
		lon = loc.getLongitude();
		
		while(continuePolling()) {
			try {
				locationUpdated(lat, lon);
				Thread.sleep(POLLING_INTERVAL_MS);
			} catch (InterruptedException e) {
				e.printStackTrace();
				break;
			}
		}
	}

	protected abstract void locationUpdated(double lat, double lon);

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
