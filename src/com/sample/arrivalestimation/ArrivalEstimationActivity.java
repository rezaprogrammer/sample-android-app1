package com.sample.arrivalestimation;

import java.io.IOException;
import java.util.Properties;

import com.google.maps.DistanceMatrixApi;
import com.google.maps.DistanceMatrixApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.DistanceMatrixElement;
import com.google.maps.model.DistanceMatrixRow;
import com.sample.app1.R;
import com.sample.app1.R.id;
import com.sample.app1.R.layout;
import com.sample.locationtracker.LocationPoler;

import android.app.Activity;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ArrivalEstimationActivity extends Activity {

	final int quota_limit = 10;
	int quota_used = 0;
	
	LocationManager locMan = null;
	LocationPoler locPoler = null;
	TextView currLocTextView = null;
	EditText destLocEditText = null;
	Button estimateButton = null;
	TextView estimatedTexView = null;
	double latitude = -1;
	double longitude = -1;
	long timeToDestination = -1;
	GeoApiContext geoContext = new GeoApiContext();
	final static String API_KEY;
	static {
		Properties prop = new Properties();
		try {
			prop.load(ArrivalEstimationActivity.class.getResourceAsStream("ArrivalEstimationActivity.properties"));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		API_KEY = prop.getProperty("Geo_ServerApiKey");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_arrival_estimation);
		currLocTextView = (TextView) findViewById(R.id.curr_loc_id);
		destLocEditText = (EditText) findViewById(R.id.dest_loc_id);
		estimateButton = (Button) findViewById(R.id.estimate_button_id);
		estimatedTexView = (TextView) findViewById(R.id.estimate_id);
		
		locMan = (LocationManager) getSystemService(LOCATION_SERVICE);
		locPoler = new LocationPoler(locMan) {
			@Override
			protected void locationUpdated(double lat, double lon) {
				updateCurrLocation(lat, lon);
			}
		};
		
		locPoler.registerAsLocationListener();
		Thread t = new Thread(locPoler);
		t.start();
	}
	
	protected void updateCurrLocation(double lat, double lon) {
		latitude = lat;
		longitude = lon;
		
		updateETA();

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				currLocTextView.setText(getCurrLocationString());
				String mark = (checkQuota() ? "" : "*");
				estimatedTexView.setText(mark + String.valueOf(timeToDestination) + "s");
			}
		});
	}

	protected void updateETA() {
		if(!incrementAndCheckQuota())
			return;
		
		geoContext.setApiKey(API_KEY);
		geoContext.setQueryRateLimit(2);
		String currLocStr = getCurrLocationString();
		String destLocStr = getDestinationString();
		if(destLocStr.trim().length() == 0) {
			timeToDestination = -1L;
			return;
		}
		
		try {
			DistanceMatrixApiRequest req =
					DistanceMatrixApi.getDistanceMatrix(geoContext, new String[]{currLocStr}, new String[]{destLocStr});
			DistanceMatrix results = req.await();
			for(DistanceMatrixRow result : results.rows) {
				for(DistanceMatrixElement element : result.elements) {
					System.out.println(element.distance + ", " + element.duration + ", " + element.status);
					timeToDestination = element.duration.inSeconds;
					return;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			timeToDestination = -2L;
		}
		timeToDestination = -3L;
		return;
	}

	private boolean checkQuota() {
		if(quota_used >= quota_limit)
			return false;
		return true;
	}

	private boolean incrementAndCheckQuota() {
		++quota_used;
		return checkQuota();
	}

	private String getDestinationString() {
		return destLocEditText.getText().toString();
	}

	private String getCurrLocationString() {
		return String.valueOf(latitude) + ", " + String.valueOf(longitude);
	}
	
	public void onClick(View view) {
		switch(view.getId()) {
		case R.id.estimate_button_id:
			updateETA();
			break;
			
		case R.id.reset_map_quota_button_id:
			quota_used = 0;
			break;
		}
	}
}
