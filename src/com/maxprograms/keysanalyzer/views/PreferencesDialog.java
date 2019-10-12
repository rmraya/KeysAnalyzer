package com.maxprograms.keysanalyzer.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import java.io.IOException;

import com.maxprograms.keysanalyzer.KeysAnalyzer;
import com.maxprograms.utils.Locator;

public class PreferencesDialog {

	private Shell shell;
	private Display display;
	private Text text;

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

		Composite top = new Composite(shell, SWT.NONE);
		top.setLayout(new GridLayout(3, false));
		top.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label label = new Label(top, SWT.NONE);
		label.setText("XML Catalog");

		text = new Text(top, SWT.BORDER);
		GridData data = new GridData();
		data.widthHint = 400;
		text.setLayoutData(data);
		text.setText(KeysAnalyzer.getCatalog());

		Button browse = new Button(top, SWT.PUSH);
		browse.setText("Browse...");
		browse.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				FileDialog fd = new FileDialog(shell, SWT.OPEN);
				fd.setFileName(KeysAnalyzer.getCatalog());
				fd.setFilterNames(new String[] { "XML Files [*.xml]", "All Files [*.*]" });
				fd.setFilterExtensions(new String[] { "*.xml", "*.*" });
				String value = fd.open();
				if (value != null) {
					text.setText(value);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// do nothing
			}
		});

		Composite bottom = new Composite(shell, SWT.NONE);
		bottom.setLayout(new GridLayout(2, false));
		bottom.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label filler = new Label(bottom, SWT.NONE);
		filler.setText("");
		filler.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Button save = new Button(bottom, SWT.PUSH);
		save.setText("Save Settings");
		save.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				try {
					KeysAnalyzer.setCatalog(text.getText());
				} catch (IOException e) {
					MessageBox box = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
					box.setMessage(e.getMessage());
					box.open();
					e.printStackTrace();
				}
				shell.close();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// do nothing
			}
		});

		shell.pack();
	}

	public void show() {
		Locator.setLocation(shell, "PreferencesDialog");
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

}
