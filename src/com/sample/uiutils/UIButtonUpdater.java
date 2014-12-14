package com.sample.uiutils;

import android.widget.Button;

public class UIButtonUpdater extends UIUpdater {
	
	private final Button button;
	private final boolean enabled;
	
	public UIButtonUpdater(Button button, boolean enabled) {
		super();
		this.button = button;
		this.enabled = enabled;
	}
	
	@Override
	public void run() {
		button.setEnabled(enabled);
	}
}