package com.maxprograms.keysanalyzer.views;

import java.io.File;
import java.util.Date;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
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

import com.maxprograms.keysanalyzer.KeysAnalyzer;
import com.maxprograms.keysanalyzer.controllers.Analyzer;
import com.maxprograms.keysanalyzer.controllers.ILogger;
import com.maxprograms.keysanalyzer.models.Analysis;
import com.maxprograms.utils.Locator;
import com.maxprograms.widgets.AsyncLogger;
import com.maxprograms.widgets.LogPanel;

public class MapSelectorDialog implements ILogger {

	private boolean cancelled = true;
	private Shell shell;
	private Display display;
	private Text mapText;
	private AsyncLogger alogger;
	private LogPanel logger;
	private Button cancel;
	private Button analyze;
	protected Listener closeListener;
	protected Analysis analysis;
	private Text valText;
	protected String val;
	protected String mapFile;

	public MapSelectorDialog(Shell parent, int style) {
		shell = new Shell(parent, style);
		shell.setText("Analyze Map");
		shell.setImage(KeysAnalyzer.getResourceManager().getIcon());
		shell.setLayout(new GridLayout());
		display = shell.getDisplay();
		shell.addListener(SWT.Close, new Listener() {

			@Override
			public void handleEvent(Event arg0) {
				Locator.remember(shell, "MapSelectorDialog");
			}
			
		});

		alogger = new AsyncLogger(this);
		
		Composite top = new Composite(shell, SWT.NONE);
		top.setLayout(new GridLayout(3, false));
		
		Label mapLabel = new Label(top, SWT.NONE);
		mapLabel.setText("DITA Map");
		
		mapText = new Text(top, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = 300;
		mapText.setLayoutData(data);
		
		Button browseMap = new Button(top, SWT.PUSH);
		browseMap.setText("Browse...");
		browseMap.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				FileDialog fd = new FileDialog(shell, SWT.OPEN);
				fd.setFilterExtensions(new String[]{"*.ditamap", "*.bookmap", "*.*"});
				fd.setFilterNames(new String[]{"DITA Maps [*.ditamap", "DITA Bookmaps [*.bookmap]", "All Files [*.*]"});
				String map = fd.open();
				if ( map != null) {
					mapText.setText(map);
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// do nothing				
			}
		});
		
		Label valLabel = new Label(top, SWT.NONE);
		valLabel.setText("DITAVAL File");
		
		valText = new Text(top, SWT.BORDER);
		valText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Button valButton = new Button(top, SWT.PUSH);
		valButton.setText("Browse...");
		valButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				FileDialog fd = new FileDialog(shell, SWT.OPEN);
				fd.setFilterNames(new String[]{"DITAVAL Files [*.ditaval]", "All Files [*.*]"});
				fd.setFilterExtensions(new String[] {"*.ditaval", "*.*"});
				if (valText.getText() != null) {
					try {
						File f = new File(valText.getText());
						fd.setFileName(f.getName());
						fd.setFilterPath(f.getParent());
					} catch (Exception e) {
						// do nothing
					}
				}
				String val = fd.open();
				if (val != null) {
					File f = new File(val);
					if (f.exists()) {
						valText.setText(val);
					}
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// do nothing				
			}
		});

		logger = new LogPanel(shell, SWT.BORDER);
		logger.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Composite bottom = new Composite(shell, SWT.BORDER);
		bottom.setLayout(new GridLayout(3, false));
		bottom.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label filler = new Label(bottom, SWT.NONE);
		filler.setText("");
		filler.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		cancel = new Button(bottom, SWT.PUSH);
		cancel.setText("Cancel");
		cancel.setEnabled(false);
		cancel.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				cancelled = true;
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				cancelled = true;
			}
		});
		
		analyze = new Button(bottom, SWT.PUSH);
		analyze.setText("Analyze Map");
		analyze.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				mapFile = mapText.getText();
				if (mapFile == null || mapFile.equals("")) {
					MessageBox box = new MessageBox(shell, SWT.OK|SWT.ICON_WARNING);
					box.setMessage("Select DITA map.");
					box.open();
					return;
				}
				File in = new File(mapFile);		
				if (!in.exists()) {
					MessageBox box = new MessageBox(shell, SWT.OK|SWT.ICON_WARNING);
					box.setMessage("DITA map does not exist.");
					box.open();
					return;
				}
				val = valText.getText();
				if (val != null && !val.equals("")) {
					File v = new File(val);
					if (!v.exists()) {
						MessageBox box = new MessageBox(shell, SWT.OK|SWT.ICON_WARNING);
						box.setMessage("DITAVAL file does not exist.");
						box.open();
						return;
					}
					val = v.getAbsolutePath();					
				} else {
					val = null;
				}
				analysis = new Analysis(mapFile, val, new Date());
				analyze.setEnabled(false);
				cancel.setEnabled(true);
				cancelled = false;
				closeListener = new Listener() {
					
					@Override
					public void handleEvent(Event ev) {
						cancelled = true;
						ev.doit = false;
					}
				};
				shell.addListener(SWT.Close, closeListener);
				Thread thread = new Thread() {
					public void run() {
						Analyzer.analize(analysis, alogger);
					}
				};
				thread.start();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// do nothing
			}
		});

		shell.pack();
	}

	public void show() {
		Locator.position(shell, "MapSelectorDialog");
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	public boolean wasCancelled() {
		return cancelled;
	}
	
	@Override
	public void log(String message) {
		logger.log(message);
	}

	@Override
	public void setStage(String stage) {
		logger.setStage(stage);
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void logError(String error) {
		logger.logError(error);
	}

	@Override
	public Vector<String> getErrors() {
		return logger.getErrors();
	}

	@Override
	public void displayError(String string) {
		display.asyncExec(new Runnable() {

			@Override
			public void run() {
				shell.removeListener(SWT.Close, closeListener);
				logger.setStage("Process Failed");
				logger.log(string);
				analyze.setEnabled(true);
				cancel.setEnabled(false);
				MessageBox box = new MessageBox(shell, SWT.ICON_ERROR|SWT.OK);
				if (string != null) {
					box.setMessage(string);
				} else {
					box.setMessage("Unknown error. Please check logs.");
				}
				box.open();
			}
		});
	}

	@Override
	public void displaySuccess(String string) {
		display.asyncExec(new Runnable() {

			@Override
			public void run() {
				shell.removeListener(SWT.Close, closeListener);
				KeysAnalyzer.getController().addAnalysis(analysis);
				KeysAnalyzer.getMainView().loadAnalysis();
				logger.setStage("Process Finished");
				logger.log(string);
				analyze.setEnabled(true);
				cancel.setEnabled(false);
				File map = new File(mapFile);
				File out = new File(map.getParentFile(), "out");
				if (!out.exists()) {
					out.mkdirs();
				}
				File keysFolder = new File(out, "keys");
				if (!keysFolder.exists()) {
					keysFolder.mkdirs();
				}
				
				File parsedFiles = new File(keysFolder, "files.html");
				Program.launch(parsedFiles.getAbsolutePath());
			}
		});
		
	}

	public void setAnalysis(Analysis data) {
		analysis = data;
		mapText.setText(data.getMapFile());
		if (data.getDitaval() != null) {
			valText.setText(data.getDitaval());
		}
	}

}
