package com.maxprograms.keyanalyzer;

import java.io.File;
import java.io.PrintStream;

import org.eclipse.swt.widgets.Display;

import com.maxprograms.keyanalyzer.controllers.Controller;
import com.maxprograms.keyanalyzer.views.resources.ResourceManager;
import com.maxprograms.utils.Preferences;

public class KeyAnalyzer {

	private static Display display;
	private static ResourceManager resourceManager;
	private static MainView main;
	private static Controller controller;

	public static void main(String[] args) {
		Display.setAppName("DITA Keys Analyzer");
		Display.setAppVersion(Constants.VERSION);
		display = Display.getDefault();
		
		resourceManager = new ResourceManager(display);
		try {

			checkLock();
			lock();
		
			controller = new Controller();
			main = new MainView(display);
			main.show();
			if (!display.isDisposed()) {
				display.dispose();
			}
			controller.close();
			unlock();
		} catch (Exception e) {
			try {
				File log = new File(Preferences.getPreferencesDir().getParentFile(), "KeyAnalyzer_error.log"); //$NON-NLS-1$
				PrintStream stream = new PrintStream(log);
				e.printStackTrace(stream);
				stream.close();
			} catch (Exception e2) {
				e.printStackTrace();
			}
		}
	}

	private static void checkLock() {
		// TODO Auto-generated method stub
		
	}

	private static void unlock() {
		// TODO Auto-generated method stub
		
	}

	private static void lock() {
		// TODO Auto-generated method stub
		
	}

	public static ResourceManager getResourceManager() {
		return resourceManager;
	}

	public static MainView getMainView() {
		return main;
	}

	public static Controller getController() {
		return controller;
	}
}
