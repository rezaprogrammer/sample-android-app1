package com.sample.speech;

import java.io.StringWriter;
import java.util.ArrayList;

import com.sample.app1.R;
import com.sample.uiutils.UIButtonUpdater;
import com.sample.uiutils.UITextViewUpdater;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class SpeechRecognizerActivity extends Activity implements RecognitionListener {

	private static final String LOGTAG = "SpeechRecognizer";
	
	private byte[] recording = null;
	private TextView statusField = null;
	private TextView resultsField = null;
	private Button startRecognizingButton = null;
	private Button stopRecognizingButton = null;
	SpeechRecognizer recognizer = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_speech_recognizer);
	    
		startRecognizingButton = (Button) findViewById(R.id.speech_recognizer_start_button);
		stopRecognizingButton = (Button) findViewById(R.id.speech_recognizer_stop_button);
		statusField = (TextView) findViewById(R.id.speech_recognizer_status);
		resultsField = (TextView) findViewById(R.id.speech_recognizer_result);
	}

	@Override
	public void onReadyForSpeech(Bundle params) {
		updateUI("Speak...", null, false, true);
	}

	@Override
	public void onBeginningOfSpeech() {
		updateUI("Listening...", null, false, true);
	}

	@Override
	public void onRmsChanged(float rmsdB) {
		return;
	}

	@Override
	public void onBufferReceived(byte[] buffer) {
		recording = buffer;
		AudioTrack audioTrack =
				new  AudioTrack(AudioManager.STREAM_VOICE_CALL,
						8000,
						AudioFormat.CHANNEL_CONFIGURATION_MONO,
						AudioFormat.ENCODING_PCM_16BIT,
						500000,
						AudioTrack.MODE_STATIC);
		audioTrack.write(recording, 0, 500000);
        audioTrack.play();
	}

	@Override
	public void onEndOfSpeech() {
		updateUI("Analyzing...", null, false, false);
	}

	@Override
	public void onError(int error) {
		Log.e(LOGTAG, "An error occurred: " + error);
	}

	@Override
	public void onResults(Bundle results) {
		ArrayList<String> strResultsList = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
		StringWriter strWriter = new StringWriter();
		for(int i=0; i<strResultsList.size() ; i++)
			strWriter.append((i==0 ? "" : ",") + strResultsList.get(i));
		updateUI("Done.", strWriter.toString(), true, false);
	}

	@Override
	public void onPartialResults(Bundle partialResults) {
		updateUI("Half done.", null, true, false);
	}

	@Override
	public void onEvent(int eventType, Bundle params) {
		Log.i(LOGTAG, "Some events came along [" + eventType + "]: " + params.toString());
	}

	protected void updateUI(String status, String results, boolean startButtonActive, boolean stopButtonActive) {
		if(startButtonActive || stopButtonActive) {
			//Update buttons only if at least one of them is set to be active
			runOnUiThread(new UIButtonUpdater(startRecognizingButton, startButtonActive));
			runOnUiThread(new UIButtonUpdater(stopRecognizingButton, stopButtonActive));
		}
		
		if(status != null) {
			runOnUiThread(new UITextViewUpdater(statusField, status));
		}

		if(results != null) {
			runOnUiThread(new UITextViewUpdater(resultsField, results));
		}
	}
	
	public void onClick(View view) throws InterruptedException {
		switch(view.getId()) {
		case R.id.speech_recognizer_start_button:
		{
			if(!SpeechRecognizer.isRecognitionAvailable(getApplicationContext())) {
				Toast.makeText(getApplicationContext(), "Speech recognizer not available", Toast.LENGTH_LONG).show();
				
				return;
			}
			
			updateUI(null, null, false, true);
			
			Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,"en");
		    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
		    		RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		    intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
		    
		    recognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
			recognizer.setRecognitionListener(this);
		    recognizer.startListening(intent);
		    
		    Log.i(LOGTAG, "Listening started.");
		    
//		    recognizer.stopListening();
		    
			
		    break;
		}
		case R.id.speech_recognizer_stop_button:
		{
			updateUI(null, null, true, false);
			break;
		}
		
//		Log.e(LOGTAG, "Unknown clicked view: " + view);
		}
	}
}
