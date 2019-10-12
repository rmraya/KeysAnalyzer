package com.maxprograms.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;

import com.maxprograms.keysanalyzer.views.HTMLViewer;

public class CustomLink  {

	Label link;
	private String url;
	private Composite parent;

	public CustomLink(Composite parent, int style) {
		
		link = new Label(parent, style);
		
		this.parent = parent;
		
		link.setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
		link.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_LINK_FOREGROUND));
		link.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent arg0) {
				// do nothing				
			}
			
			@Override
			public void mouseDown(MouseEvent arg0) {
				displayLink();
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
				// do nothing				
			}
		});
	}
	

	protected void displayLink() {
		try {
			HTMLViewer viewer = new HTMLViewer(link.getShell());
			viewer.setTitle(link.getText());
			viewer.display(url);
			viewer.show();
			link.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_MAGENTA));
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox box = new MessageBox(parent.getShell(), SWT.ICON_ERROR);
			box.setMessage(e.getMessage());
			box.open();
		}
	}

	public void setURL(String value) {
		url = value;
	}

	public void setForeground(Color value) {
		link.setForeground(value);
	}

	public void setText(String string) {
		link.setText(string);
	}
}
