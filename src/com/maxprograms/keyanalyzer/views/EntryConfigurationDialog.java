package com.maxprograms.keyanalyzer.views;


import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.maxprograms.keyanalyzer.KeyAnalyzer;
import com.maxprograms.utils.Locator;
import com.maxprograms.xml.Element;

public class EntryConfigurationDialog extends Dialog {

	Shell shell;
	private Display display;
	boolean cancelled = true;
	Text name;
	Text uri;

	public EntryConfigurationDialog(Shell parent, final Element e) {
		super(parent,SWT.NONE);
		shell = new Shell(parent,SWT.DIALOG_TRIM|SWT.RESIZE);
		shell.setImage(KeyAnalyzer.getResourceManager().getIcon());
		shell.setText("Catalog Entry"); 
		shell.setLayout(new GridLayout());
		shell.addListener(SWT.Close, new Listener() {

			@Override
			public void handleEvent(Event arg0) {
				Locator.remember(shell, "EntryConfigurationDialog");
			}
		});
		display = shell.getDisplay();
		
		Composite top = new Composite(shell,SWT.NONE);
		top.setLayout(new GridLayout(2,false));
		top.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL|GridData.FILL_HORIZONTAL));
		
		Label label = new Label(top,SWT.NONE);
		if (e.getName().equals("public")) { 
			label.setText("Public ID"); 
		}
		if (e.getName().equals("system")) { 
			label.setText("System ID"); 
		}
		if (e.getName().equals("uri")) { 
			label.setText("URI Name"); 
		}
		
		name = new Text(top,SWT.BORDER);
		GridData data = new GridData(GridData.GRAB_HORIZONTAL|GridData.FILL_HORIZONTAL);
		data.widthHint = 250;
		name.setLayoutData(data);
		if (e.getName().equals("public")) { 
			name.setText(e.getAttributeValue("publicId","")); 
		} else if (e.getName().equals("system")) { 
			name.setText(e.getAttributeValue("systemId","")); 
		} else if (e.getName().equals("uri")) { 
			name.setText(e.getAttributeValue("name","")); 
		}
		
		Label uriLabel = new Label(top,SWT.NONE);
		uriLabel.setText("URI"); 
		
		uri = new Text(top,SWT.BORDER);
		uri.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL|GridData.FILL_HORIZONTAL));
		uri.setText(e.getAttributeValue("uri","")); 

		Composite bottom = new Composite(shell,SWT.NONE);
		bottom.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL|GridData.FILL_HORIZONTAL));
		bottom.setLayout(new GridLayout(2,false));
		
		Label filler = new Label(bottom, SWT.NONE);
		filler.setText("");
		filler.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Button okButton = new Button(bottom,SWT.PUSH);
		okButton.setText("Save"); 
		okButton.addSelectionListener(new SelectionAdapter(){

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (name.getText().equals("")) { 
					MessageBox box = new MessageBox(shell,SWT.ICON_WARNING|SWT.OK);
					box.setMessage("Enter a property value."); 
					box.open();
					return;
				}
				if (uri.getText().equals("")) { 
					MessageBox box = new MessageBox(shell,SWT.ICON_WARNING|SWT.OK);
					box.setMessage("Enter an URI."); 
					box.open();
					return;
				}
				if (e.getName().equals("public")) { 
					e.setAttribute("publicId",name.getText()); 
				}
				if (e.getName().equals("system")) { 
					e.setAttribute("systemId",name.getText()); 
				}
				if (e.getName().equals("uri")) { 
					e.setAttribute("name",name.getText()); 
				}
				e.setAttribute("uri",uri.getText()); 
				cancelled = false;
				shell.close();
			}
		});
		
		shell.pack();
	}

	public void show() {
		Locator.position(shell, "EntryConfigurationDialog");
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


}
