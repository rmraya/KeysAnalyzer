package com.maxprograms.keysanalyzer.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.maxprograms.keysanalyzer.KeysAnalyzer;
import com.maxprograms.utils.Locator;

public class PreferencesDialog  {

	private Shell shell;
	private Display display;

	public PreferencesDialog(Shell parent, int style) {
		shell = new Shell(parent, style);
		shell.setImage(KeysAnalyzer.getResourceManager().getIcon());
		shell.setText("XML Catalog"); 
		shell.setLayout(new GridLayout());
		shell.addListener(SWT.Close, new Listener() {

			@Override
			public void handleEvent(Event arg0) {
				Locator.remember(shell, "PreferencesDialog");
			}
		});
		display = shell.getDisplay();
		
		new XmlCatalogView(shell, SWT.NONE);
		shell.pack();
	}

	public void show() {
		Locator.position(shell, "PreferencesDialog");
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

}
