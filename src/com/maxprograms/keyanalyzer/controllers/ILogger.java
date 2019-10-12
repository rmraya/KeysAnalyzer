package com.maxprograms.keyanalyzer.controllers;

import java.util.Vector;

public interface ILogger {

	public void log(String message);
	public void setStage(String stage);
	public boolean isCancelled();
	public void logError(String error);
	public Vector<String> getErrors();
	public void displayError(String string);
	public void displaySuccess(String string);
}
