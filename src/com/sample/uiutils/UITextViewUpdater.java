package com.sample.uiutils;

import android.widget.TextView;

public class UITextViewUpdater extends UIUpdater {
	
	private final TextView tView;
	private final String text;
	
	public UITextViewUpdater(TextView tView, String text) {
		super();
		this.tView = tView;
		this.text = text;
	}
	
	@Override
	public void run() {
		tView.setText(text);
	}
}