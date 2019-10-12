package com.maxprograms.widgets;

import java.util.Vector;

import org.eclipse.swt.widgets.Display;

import com.maxprograms.keyanalyzer.controllers.ILogger;


public class AsyncLogger implements ILogger {
	
	private ILogger parent;

	private Vector<String> errors;

	private Display display;
	
	public AsyncLogger(ILogger parent) {
		this.parent = parent;
		display = Display.getCurrent();
	}

	@Override
	public synchronized void log(String message) {
		display.asyncExec(new Runnable() {

			@Override
			public void run() {
				parent.log(message);				
			}
		});
	}

	@Override
	public synchronized void setStage(String stage) {
		display.asyncExec(new Runnable() {
			
			@Override
			public void run() {
				parent.setStage(stage);
			}
		});
	}

	@Override
	public synchronized boolean isCancelled() {
		return parent.isCancelled();
	}

	@Override
	public synchronized void logError(String error) {
		if (errors == null) {
			errors = new Vector<String>();
		}
		errors.add(error);
	}

	@Override
	public Vector<String> getErrors() {
		if (errors == null) {
			errors = new Vector<String>();
		}
		return errors;
	}

	@Override
	public void displayError(String string) {
		parent.displayError(string);
	}

	@Override
	public void displaySuccess(String string) {
		parent.displaySuccess(string);
	}

}
