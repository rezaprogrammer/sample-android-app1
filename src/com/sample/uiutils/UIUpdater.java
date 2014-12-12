package com.sample.uiutils;

abstract class UIUpdater implements Runnable {
	
	public UIUpdater() { }
	
	public abstract void run();
}