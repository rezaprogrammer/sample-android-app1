package com.sample.app1;

import com.sample.utils.Utils;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
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
	
	public void pressMeOnClick(View view) {
		EditText textArea = (EditText) findViewById(R.id.nameEntryTextArea);
		String name = textArea.getText().toString();
		if(Utils.isEmpty(name)) {
			Toast.makeText(this, "Please enter a name",
		            Toast.LENGTH_LONG).show();
			return;
		}
		
		TextView textView = (TextView) findViewById(R.id.helloTextView);
		textView.setText("Hello " + name + "!");
	}
	
	public void onClick(View view) {
		switch(view.getId()) {
		case R.id.pressMe:
			pressMeOnClick(view);
			break;
		}
	}
}
