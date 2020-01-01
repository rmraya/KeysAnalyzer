/*********************************************************************** 

Copyright (c) 2016-2020 - Maxprograms,  http://www.maxprograms.com/

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
***********************************************************************/
package com.maxprograms.keysanalyzer.views;

import java.text.MessageFormat;

import com.maxprograms.keysanalyzer.Constants;
import com.maxprograms.keysanalyzer.KeysAnalyzer;
import com.maxprograms.utils.Locator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class AboutBox {

	private Shell shell;
	private Display display;

	public AboutBox(Shell parent, int style) {
		shell = new Shell(parent, style);
		shell.setImage(KeysAnalyzer.getResourceManager().getIcon());
		MessageFormat mf = new MessageFormat("Version {0} - Build {1}");
		shell.setText(mf.format(new Object[] { Constants.VERSION, Constants.BUILD }));
		GridLayout shellLayout = new GridLayout();
		shellLayout.marginWidth = 0;
		shellLayout.marginHeight = 0;
		shell.setLayout(shellLayout);
		shell.addListener(SWT.Close, new Listener() {

			@Override
			public void handleEvent(Event arg0) {
				Locator.remember(shell, "AboutBox");
			}
		});
		display = shell.getDisplay();
		shell.setBackground(display.getSystemColor(SWT.COLOR_WHITE));

		Label image = new Label(shell, SWT.NONE);
		image.setAlignment(SWT.CENTER);
		image.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		image.setImage(KeysAnalyzer.getResourceManager().getLogo());
		image.setBackground(display.getSystemColor(SWT.COLOR_WHITE));

		Label appName = new Label(shell, SWT.NONE);
		appName.setText("KeysAnalyzer");
		appName.setAlignment(SWT.CENTER);
		appName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		appName.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
		Font font = appName.getFont();
		FontData[] fontData = font.getFontData();
		for (int i = 0; i < fontData.length; i++) {
			fontData[i].setHeight(24);
		}
		Font newFont = new Font(display, fontData);
		appName.setFont(newFont);

		Label copyright = new Label(shell, SWT.NONE);
		copyright.setText("Copyright \u00A9 2016-2020 Maxprograms");
		copyright.setAlignment(SWT.CENTER);
		copyright.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		copyright.setBackground(display.getSystemColor(SWT.COLOR_WHITE));

		Group info = new Group(shell, SWT.NONE);
		info.setText("System Information");
		info.setLayout(new GridLayout());

		Label os1 = new Label(info, SWT.NONE);
		MessageFormat mf3 = new MessageFormat("Operating System: {0} ({1})");
		os1.setText(mf3.format(new Object[] { System.getProperty("os.name"), System.getProperty("os.version") }));

		Label java1 = new Label(info, SWT.NONE);
		MessageFormat mf1 = new MessageFormat("Java Version: {0} {1}");
		java1.setText(
				mf1.format(new Object[] { System.getProperty("java.version"), System.getProperty("java.vendor") }));

		Label java2 = new Label(info, SWT.NONE);
		MessageFormat mf2 = new MessageFormat("Maximum / Allocated / Free JVM Memory: {0} / {1} / {2}");
		java2.setText(mf2.format(new Object[] { Runtime.getRuntime().maxMemory() / (1024 * 1024) + "MB",
				Runtime.getRuntime().totalMemory() / (1024 * 1024) + "MB",
				Runtime.getRuntime().freeMemory() / (1024 * 1024) + "MB" }));

		Composite bottom = new Composite(shell, SWT.NONE);
		bottom.setLayout(new GridLayout(2, false));
		bottom.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label filler = new Label(bottom, SWT.NONE);
		filler.setText("");
		filler.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Button licenses = new Button(bottom, SWT.PUSH);
		licenses.setText("Licenses");
		licenses.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				Licenses licensesDialog = new Licenses(shell, SWT.CLOSE);
				licensesDialog.show();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// do nothing
			}
		});

		shell.pack();
	}

	public void show() {
		Locator.setLocation(shell, "AboutBox");
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

}
