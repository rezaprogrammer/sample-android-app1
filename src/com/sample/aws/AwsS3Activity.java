package com.sample.aws;

import java.io.IOException;
import java.io.InputStream;

import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.org.joda.time.DateTime;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.util.StringInputStream;
import com.sample.app1.R;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class AwsS3Activity extends Activity {

	AmazonS3 s3 = null;
	protected static final String BUCKET_NAME = "sample-android-app1";
	protected static final String LOGTAG = AwsS3Activity.class.getName();
	EditText s3textbox;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_aws_s3);
		s3textbox = (EditText) findViewById(R.id.s3_texbox_id);
		
		initializeS3();
	}
	
	private void initializeS3() {
		try {
			PropertiesCredentials prop =
					new PropertiesCredentials(AwsS3Activity.class.getResourceAsStream("AwsS3Activity.properties"));
			s3 = new AmazonS3Client(prop);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
        Region usWest2 = Region.getRegion(Regions.US_WEST_2);
        s3.setRegion(usWest2);
	}

	public void onClick(View view) {
		switch(view.getId()) {
		case R.id.save_button_id:
			String str = s3textbox.getText().toString();
			AsyncTask<String, Void, String> task = new AsyncTask<String, Void, String>() {
				@Override
				protected String doInBackground(String... params) {
					String str = params[0];
					return save(str);
				}
				
				@Override
				protected void onPostExecute(String result) {
					super.onPostExecute(result);
					Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
				}
			};
			task.execute(new String[]{str});
			break;
		}
	}
	
	protected String save(String str) {
		String key = new DateTime().toString();
		PutObjectRequest putReq;
		try {
			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentLength(str.length());
			metadata.setCacheControl("no-cache,no-store");
			metadata.setContentType("text/plain");
			InputStream stream = new StringInputStream(str);
			putReq = new PutObjectRequest(BUCKET_NAME, key, stream, metadata);
	        PutObjectResult putResult = s3.putObject(putReq);
	        Log.i(LOGTAG, "Version id: " + putResult.getVersionId());
	        Log.i(LOGTAG, "ETav: " + putResult.getETag());
	        return "Saving succeeded.";
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(LOGTAG, "Saving to S3 failed", e);
			return "Saving failed.";
		}
	}
}
