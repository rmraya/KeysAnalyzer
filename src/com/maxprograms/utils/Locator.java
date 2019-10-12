package com.maxprograms.utils;

import java.io.IOException;
import java.util.Hashtable;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

import com.maxprograms.keyanalyzer.Constants;


public class Locator {
	
	public static void setLocation(Shell shell, String type) {
		try {
			Hashtable<String, String> values = Preferences.getInstance(Constants.PREFERENCES).get(type);
			if (values.size() > 0) {
				Point location = new Point(Integer.parseInt(values.get("X")), Integer.parseInt(values.get("Y"))); //$NON-NLS-1$ //$NON-NLS-2$
				shell.setLocation(location);
			}
		} catch (IOException ioe){
			ioe.printStackTrace();
		}
	}

	public static void position(Shell shell, String type) {
		try {
			Hashtable<String, String> values = Preferences.getInstance(Constants.PREFERENCES).get(type);
			if (values.size() > 0) {
				Point location = new Point(Integer.parseInt(values.get("X")), Integer.parseInt(values.get("Y"))); //$NON-NLS-1$ //$NON-NLS-2$
				shell.setLocation(location);
				Point size = new Point(Integer.parseInt(values.get("Width")), Integer.parseInt(values.get("Height"))); //$NON-NLS-1$ //$NON-NLS-2$
				shell.setSize(size);
			}
		} catch (IOException ioe){
			ioe.printStackTrace();
		}
	}

	public static void remember(Shell shell, String type) {
		try {
			Hashtable<String, String> values = new Hashtable<String, String>();
			values.put("X", "" + shell.getLocation().x); //$NON-NLS-1$ //$NON-NLS-2$
			values.put("Y", "" + shell.getLocation().y); //$NON-NLS-1$ //$NON-NLS-2$
			values.put("Width", "" + shell.getSize().x); //$NON-NLS-1$ //$NON-NLS-2$
			values.put("Height", "" + shell.getSize().y); //$NON-NLS-1$ //$NON-NLS-2$
			Preferences.getInstance(Constants.PREFERENCES).save(type, values);
		} catch (IOException ioe){
			ioe.printStackTrace();
		}
	}

}
