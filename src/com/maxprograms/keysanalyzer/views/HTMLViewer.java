package com.maxprograms.keysanalyzer.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.maxprograms.keysanalyzer.KeysAnalyzer;
import com.maxprograms.utils.Locator;

public class HTMLViewer extends Dialog {

	private Shell shell;
	private Display display;
	private Browser browser;

	public HTMLViewer(Shell parent) throws Exception {
		super(parent,SWT.NONE);
		shell = new Shell(parent,SWT.CLOSE|SWT.TITLE|SWT.MODELESS|SWT.BORDER|SWT.RESIZE);
		shell.setImage(KeysAnalyzer.getResourceManager().getIcon());
		display = shell.getDisplay();
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		shell.setLayout(layout);
		shell.addListener(SWT.Close, new Listener() {
			@Override
			public void handleEvent(Event event) {
				Locator.remember(shell, "HTMLViewer");
			}
		});

		try {
			if (System.getProperty("file.separator").equals("/")) { 
				browser = new Browser(shell, SWT.WEBKIT);			
			} else {
				browser = new Browser(shell, SWT.NONE);	
			}
		} catch (SWTError e) {
			e.printStackTrace();
			String message = ""; 
			if (System.getProperty("file.separator").equals("/") ) { 
				if (System.getProperty("os.name").startsWith("Mac")) { 
					// Mac
					message = "Error embedding browser. Check Safari's configuration."; 
				} else {
					// Linux
					message = "Error embedding browser. WebKitGTK+ 1.2.x is required.";  
				}
			} else {
				message = "Error embedding browser."; 
			}
			
			throw new Exception(message);
		}
		browser.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL|GridData.GRAB_VERTICAL | GridData.FILL_BOTH));
		
		shell.addKeyListener(new KeyListener() {
			
			@Override
			public void keyReleased(KeyEvent arg0) {
				// do nothing 				
			}
			
			@Override
			public void keyPressed(KeyEvent arg0) {
				if (arg0.keyCode == SWT.ESC) {
					shell.dispose();
				}
			}
		});
	}
	
	public void show() {
		Locator.position(shell, "HTMLViewer");
		shell.open();
		while (!shell.isDisposed()) {
			if ( !display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	public void display(String string) {
		browser.setUrl(string);
	}

	public void setTitle(String title) {
		shell.setText(title);
	}
}
