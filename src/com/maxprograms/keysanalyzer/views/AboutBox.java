package com.maxprograms.keysanalyzer.views;

import java.io.File;
import java.net.MalformedURLException;
import java.text.MessageFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.maxprograms.keysanalyzer.Constants;
import com.maxprograms.keysanalyzer.KeysAnalyzer;
import com.maxprograms.utils.Locator;
import com.maxprograms.utils.Preferences;
import com.maxprograms.widgets.CustomLink;

public class AboutBox {

	private Shell shell;
	private Display display;

	public AboutBox(Shell parent, int style) {
		shell = new Shell(parent, style);
		shell.setImage(KeysAnalyzer.getResourceManager().getIcon());
		MessageFormat mf = new MessageFormat("Version {0} - Build {1}");
		shell.setText(mf.format(new Object[]{Constants.VERSION, Constants.BUILD}));
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
		appName.setText("DITA Keys Analyzer"); 
		appName.setAlignment(SWT.CENTER);
		appName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		appName.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
		Font font = appName.getFont();
		FontData[] fontData =  font.getFontData();
		for (int i = 0; i < fontData.length; i++) {
			fontData[i].setHeight(24);
		}
		Font newFont = new Font(display, fontData);
		appName.setFont(newFont);
		
		Label copyright = new Label(shell, SWT.NONE);
		copyright.setText("Copyright \u00A9 2016-2019 Maxprograms"); 
		copyright.setAlignment(SWT.CENTER);
		copyright.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		copyright.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
		
		CTabFolder folder = new CTabFolder(shell, SWT.BORDER);
		folder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		CTabItem systemTab = new CTabItem(folder, SWT.NONE);
		systemTab.setText("System Information");
		
		Composite info = new Composite(folder, SWT.NONE);
		info.setLayout(new GridLayout());
		systemTab.setControl(info);
		
		Label os1 = new Label(info,SWT.NONE);
		MessageFormat mf3 = new MessageFormat("Operating System: {0} ({1})"); 
		os1.setText(mf3.format(new Object[]{System.getProperty("os.name"),System.getProperty("os.version")}));  

		Label java1 = new Label(info,SWT.NONE);
		MessageFormat mf1 = new MessageFormat("Java Version: {0} {1}"); 
		java1.setText(mf1.format(new Object[]{System.getProperty("java.version"),System.getProperty("java.vendor")}));  
		
		Label java2 = new Label(info,SWT.NONE);
		MessageFormat mf2 = new MessageFormat("Maximum / Allocated / Free JVM Memory: {0} / {1} / {2}"); 
		java2.setText(mf2.format(new Object[]{Runtime.getRuntime().maxMemory()/(1024*1024) + "MB", 
				Runtime.getRuntime().totalMemory()/(1024*1024) + "MB", 
				Runtime.getRuntime().freeMemory()/(1024*1024) + "MB"})); 
		
		try {
			Label userData = new Label(info, SWT.NONE);
			MessageFormat mf4 = new MessageFormat("Data Folder: {0}");
			userData.setText(mf4.format(new Object[] {Preferences.getPreferencesDir()}));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		CTabItem licensesTab = new CTabItem(folder, SWT.NONE);
		licensesTab.setText("Licenses");
		
		Composite licenses = new Composite(folder, SWT.NONE);
		licenses.setLayout(new GridLayout(2, false));
		licensesTab.setControl(licenses);
	
		Label conversa = new Label(licenses, SWT.NONE);
		conversa.setText("DITA Keys Analyzer");
		
		CustomLink conversaLink = new CustomLink(licenses, SWT.NONE);
		conversaLink.setText("MIT License");
		try {
			conversaLink.setURL(new File("lib/licenses/keyanalizer.txt").toURI().toURL().toString()); 
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		Label java = new Label(licenses, SWT.NONE);
		java.setText("Java Runtime Environment"); 
		
		CustomLink javaLink = new CustomLink(licenses, SWT.NONE);
		javaLink.setText("GPL2 With Classpath Exception"); 
		try {
			javaLink.setURL(new File("lib/licenses/Java.html").toURI().toURL().toString()); 
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		Label swt = new Label(licenses, SWT.NONE);
		swt.setText("SWT"); 
		
		CustomLink swtLink = new CustomLink(licenses, SWT.NONE);
		swtLink.setText("Eclipse Public License Version 1.0"); 
		try {
			swtLink.setURL(new File("lib/licenses/EclipsePublicLicense1.0.html").toURI().toURL().toString()); 
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		Label mapDB = new Label(licenses, SWT.NONE);
		mapDB.setText("MapDB"); 
		
		CustomLink mapdbLink = new CustomLink(licenses, SWT.NONE);
		mapdbLink.setText("Apache License 2.0"); 
		try {
			mapdbLink.setURL(new File("lib/licenses/Apache2.0.html").toURI().toURL().toString()); 
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		Label filler = new Label(licenses, SWT.NONE);
		filler.setText("");
		
		folder.setSelection(systemTab);
		systemTab.getControl().setFocus();
		
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
