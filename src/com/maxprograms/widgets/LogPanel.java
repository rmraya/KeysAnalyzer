package com.maxprograms.widgets;

import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import com.maxprograms.keyanalyzer.controllers.ILogger;

public class LogPanel extends Composite implements ILogger {

	private Display display;
	private Label stage;
	private StyledText log;
	private String home = System.getProperty("user.home"); //$NON-NLS-1$
	private Vector<String> errors;

	public LogPanel(Composite parent, int style) {
		super(parent, style);
		
		setLayout(new GridLayout());
		display = parent.getDisplay();		
		
		stage = new Label(this, SWT.NONE);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = 300;
		stage.setLayoutData(data);
		stage.setBackground(display.getSystemColor(SWT.COLOR_LIST_SELECTION));

		log = new StyledText(this, SWT.WRAP|SWT.READ_ONLY);
		GridData labelData = new GridData(GridData.FILL_HORIZONTAL);
		labelData.heightHint = log.getLineHeight() * 4;
		log.setLayoutData(labelData);
		
		setBackground(log.getBackground());
	}

	@Override
	public void log(String message) {
		if (message.indexOf(home) != -1) {
			message = replaceAllHome(message);
		}
		log.setText(message);
		display.update();
		display.sleep();
	}
	
	private String replaceAllHome(String message) {
		int start = message.indexOf(home);
		String result = message.substring(0, start) + "~";
		message = message.substring(start + home.length());
		start = message.indexOf(home);
		while (start != -1) {
			result = result + message.substring(0, start) + "~";
			message = message.substring(start + home.length());
			start = message.indexOf(home);
		}
		return result + message;
	}

	@Override
	public void setStage(String value) {
		stage.setText(value);
		display.update();
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public void logError(String error) {
		if (errors == null) {
			errors = new Vector<String>();
		}
		errors.add(error);
	}

	@Override
	public Vector<String> getErrors() {
		return errors;
	}

	@Override
	public void displayError(String string) {
		// do nothing		
	}

	@Override
	public void displaySuccess(String string) {
		// do nothing		
	}

}
