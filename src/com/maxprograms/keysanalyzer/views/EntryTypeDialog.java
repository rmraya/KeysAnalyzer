package com.maxprograms.keysanalyzer.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import com.maxprograms.keysanalyzer.KeysAnalyzer;
import com.maxprograms.utils.Locator;

public class EntryTypeDialog  {
	
	public static final int PUBLIC = 0;
	public static final int SYSTEM = 1;
	public static final int URI = 2;
	public static final int nextCatalog = 3;
	
	Shell shell;
	private Display display;
	boolean cancelled = true;
	int type = PUBLIC;
	
	public EntryTypeDialog(Shell parent) {
		shell = new Shell(parent,SWT.DIALOG_TRIM);
		shell.setImage(KeysAnalyzer.getResourceManager().getIcon());
		shell.setLayout(new GridLayout());
		shell.addListener(SWT.Close, new Listener() {

			public void handleEvent(Event arg0) {
				Locator.remember(shell, "EntryTypeDialog");
			}
		});
		display = shell.getDisplay();
		
		Group group = new Group(shell,SWT.APPLICATION_MODAL);
		group.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL|GridData.FILL_HORIZONTAL));
		group.setLayout(new GridLayout());
		group.setText("Entry Type");
		
		Button pub = new Button(group,SWT.RADIO);
		pub.setText("PUBLIC"); 
		pub.setSelection(true);
		pub.addSelectionListener(new SelectionAdapter(){

			public void widgetSelected(SelectionEvent arg0) {
				type = PUBLIC;
			}
		});		
		
		Button sys = new Button(group,SWT.RADIO);
		sys.setText("SYSTEM"); 
		sys.addSelectionListener(new SelectionAdapter(){

			public void widgetSelected(SelectionEvent arg0) {
				type = SYSTEM;
			}
		});
		
		Button uri = new Button(group,SWT.RADIO);
		uri.setText("URI"); 
		uri.addSelectionListener(new SelectionAdapter(){

			public void widgetSelected(SelectionEvent arg0) {
				type = URI;
			}
		});
		
		Button next = new Button(group,SWT.RADIO);
		next.setText("nextCatalog"); 
		next.addSelectionListener(new SelectionAdapter(){
			
			public void widgetSelected(SelectionEvent arg0) {
				type = nextCatalog;
			}
		});
		
		
		Composite bottom = new Composite(shell,SWT.NONE);
		bottom.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL|GridData.FILL_HORIZONTAL));
		bottom.setLayout(new GridLayout(2,false));
		
		Label filler = new Label(bottom, SWT.NONE);
		filler.setText("");
		filler.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Button okButton = new Button(bottom,SWT.PUSH);
		okButton.setText("Add Entry");
		okButton.addSelectionListener(new SelectionAdapter(){

			public void widgetSelected(SelectionEvent arg0) {
				if (type == -1) {
					MessageBox box = new MessageBox(shell,SWT.ICON_WARNING|SWT.OK);
					box.setMessage("Select an entry type.");
					box.open();
					return;
				}
				cancelled = false;
				shell.close();
			}
		});
		
		shell.pack();
	}
	
	public void show() {
		Locator.setLocation(shell, "EntryTypeDialog");
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
	
	public int getType() {
		return type;
	}
}
