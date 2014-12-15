package com.sample.aws;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.amazonaws.services.kinesis.model.DescribeStreamRequest;
import com.amazonaws.services.kinesis.model.DescribeStreamResult;
import com.amazonaws.services.kinesis.model.GetRecordsRequest;
import com.amazonaws.services.kinesis.model.GetRecordsResult;
import com.amazonaws.services.kinesis.model.GetShardIteratorRequest;
import com.amazonaws.services.kinesis.model.GetShardIteratorResult;
import com.amazonaws.services.kinesis.model.PutRecordRequest;
import com.amazonaws.services.kinesis.model.PutRecordResult;
import com.amazonaws.services.kinesis.model.Record;
import com.amazonaws.services.kinesis.model.Shard;
import com.sample.app1.R;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class AwsKinesisActivity extends Activity {

	AmazonKinesis kinesis = null;
	protected static final String STREAM_NAME = "sample-android-app1";
	protected static final String LOGTAG = AwsKinesisActivity.class.getName();
	EditText kinesistextbox;
	TextView kinesisListTexbox;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_aws_kinesis);
		kinesistextbox = (EditText) findViewById(R.id.kinesis_texbox_id);
		kinesisListTexbox = (TextView) findViewById(R.id.kinesis_list_id);
		
		initializeKinesis();
	}
	
	private void initializeKinesis() {
		try {
			PropertiesCredentials prop =
					new PropertiesCredentials(AwsKinesisActivity.class.getResourceAsStream("AwsKinesisActivity.properties"));
			kinesis = new AmazonKinesisClient(prop);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
        Region usWest2 = Region.getRegion(Regions.US_WEST_2);
        kinesis.setRegion(usWest2);
	}

	public void onClick(View view) {
		switch(view.getId()) {
		case R.id.save_button_id:
			String str = kinesistextbox.getText().toString();
			new AsyncTask<String, Void, String>() {
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
			}.execute(new String[]{str});
			break;
			
		case R.id.list_button_id:
			new AsyncTask<TextView, Void, String>() {
				TextView resultTexbox;
				
				@Override
				protected String doInBackground(TextView... params) {
					resultTexbox = params[0];
					return list();
				}
				
				@Override
				protected void onPostExecute(String result) {
					super.onPostExecute(result);
					resultTexbox.setText(result);
				}
			}.execute(new TextView[]{kinesisListTexbox});
			break;
		}
	}
	
	String sequenceNumberOfPreviousRecord = "0";
	
	protected String save(String str) {
		try {
			PutRecordRequest putRecordRequest = new PutRecordRequest();
			putRecordRequest.setStreamName(STREAM_NAME);
			putRecordRequest.setData(ByteBuffer.wrap( str.getBytes() ));
			putRecordRequest.setPartitionKey(String.valueOf(str.hashCode()));  
			putRecordRequest.setSequenceNumberForOrdering( sequenceNumberOfPreviousRecord );
			PutRecordResult putRecordResult = kinesis.putRecord( putRecordRequest );
			sequenceNumberOfPreviousRecord = putRecordResult.getSequenceNumber();
	        return "Saving succeeded: " + sequenceNumberOfPreviousRecord;
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(LOGTAG, "Saving to stream failed", e);
			return "Saving failed.";
		}
	}
	
	protected String list() {
		try {
			DescribeStreamRequest describeStreamRequest = new DescribeStreamRequest();
			describeStreamRequest.setStreamName(STREAM_NAME);
			List<Shard> shards = new ArrayList<Shard>();
			String exclusiveStartShardId = null;
		    describeStreamRequest.setExclusiveStartShardId(exclusiveStartShardId);
		    DescribeStreamResult describeStreamResult = kinesis.describeStream(describeStreamRequest);
		    shards.addAll( describeStreamResult.getStreamDescription().getShards() );
		    exclusiveStartShardId = shards.get(shards.size() - 1).getShardId();
			        
			GetShardIteratorRequest getShardIteratorRequest = new GetShardIteratorRequest();
			getShardIteratorRequest.setStreamName(STREAM_NAME);
			getShardIteratorRequest.setShardId(exclusiveStartShardId);
			getShardIteratorRequest.setShardIteratorType("TRIM_HORIZON");

			GetShardIteratorResult getShardIteratorResult = kinesis.getShardIterator(getShardIteratorRequest);
			String shardIterator = getShardIteratorResult.getShardIterator();
			  
			GetRecordsRequest getRecordsRequest = new GetRecordsRequest();
			getRecordsRequest.setShardIterator(shardIterator);
			getRecordsRequest.setLimit(25);

			GetRecordsResult getRecordsResult = kinesis.getRecords(getRecordsRequest);
			List<Record> records = getRecordsResult.getRecords();
			StringWriter strWriter = new StringWriter();
			for(Record r : records) {
				strWriter.append(new String(r.getData().array()) + "\n");
			}
	        return strWriter.toString();
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(LOGTAG, "Saving to stream failed", e);
			return "Saving failed.";
		}
	}
}
