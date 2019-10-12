package com.maxprograms.keysanalyzer.views;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.xml.sax.SAXException;

import com.maxprograms.keysanalyzer.Constants;
import com.maxprograms.keysanalyzer.controllers.FileUtils;
import com.maxprograms.xml.Document;
import com.maxprograms.xml.Element;
import com.maxprograms.xml.SAXBuilder;
import com.maxprograms.xml.XMLOutputter;

public class XmlCatalogView extends Composite {

	private static Document catalogDoc;
	private Table catalogTable;
	private Vector<Element> holder;
	private int count;
	
	public XmlCatalogView(Composite parent, int style) {
		super(parent, style);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		setLayout(layout);
		setLayoutData(new GridData(GridData.FILL_BOTH));
		
		try {
			loadCatalogue(Constants.CATALOG);
		} catch (SAXException | IOException | ParserConfigurationException e1) {
			e1.printStackTrace();
			MessageBox box = new MessageBox(getShell(), SWT.ICON_ERROR|SWT.OK);
			box.setMessage("There was an error loading catalog.");
			box.open();
			getShell().close();
		}

		catalogTable = new Table(this, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.SINGLE |SWT.FULL_SELECTION);
		GridData tableData = new GridData(GridData.FILL_BOTH);
		tableData.heightHint = 10 * catalogTable.getItemHeight();
		tableData.widthHint = 650;
		catalogTable.setLayoutData(tableData);
		catalogTable.setLinesVisible(true);
		catalogTable.setHeaderVisible(true);
		catalogTable.addMouseListener(new MouseAdapter(){
            @Override
			public void mouseDoubleClick(MouseEvent e) {
            	editCatalogEntry();
            }
        });
		

		final TableColumn publicId = new TableColumn(catalogTable, SWT.NONE);
		publicId.setText("Type"); 
		
		final TableColumn systemId = new TableColumn(catalogTable, SWT.NONE);
		systemId.setText("Value"); 
		
		final TableColumn dtdFile = new TableColumn(catalogTable, SWT.NONE);
		dtdFile.setText("URI"); 
		
		fillCatalogTable();

		//
		// Buttons
		//

		Composite catalogBottom = new Composite(this, SWT.NONE);
		catalogBottom.setLayout(new GridLayout(4, false));
		catalogBottom.setLayoutData(
			new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));

		Label catalogFiller = new Label(catalogBottom, SWT.NONE);
		catalogFiller.setText("");
		catalogFiller.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Button addButton = new Button(catalogBottom, SWT.PUSH);
		addButton.setText("Add Catalog Entry"); 
		addButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				addCatalogEntry();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// do nothing				
			}
		});

		Button editButton = new Button(catalogBottom, SWT.PUSH);
		editButton.setText("Edit Catalog Entry"); 
		editButton.addSelectionListener(new SelectionListener(){

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				editCatalogEntry();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// do nothing				
			}
		});

		Button removeButton = new Button(catalogBottom, SWT.PUSH);
		removeButton.setText("Remove Catalog Entry"); 
		removeButton.addSelectionListener(new SelectionAdapter(){

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				deleteCatalogEntry();
			}
		});


		addPaintListener(new PaintListener(){

			@Override
			public void paintControl(PaintEvent arg0) {
				publicId.setWidth(catalogTable.getClientArea().width * 2/10 );
				systemId.setWidth(catalogTable.getClientArea().width * 4/10);
				dtdFile.setWidth(catalogTable.getClientArea().width * 4/10);
			}
		});
		
		
		
		
	}

	private static void loadCatalogue(String catalogueFile) throws SAXException, IOException, ParserConfigurationException {
		SAXBuilder builder = new SAXBuilder();
		catalogDoc = builder.build(catalogueFile);
	}

	private static void saveCatalog() throws UnsupportedEncodingException, FileNotFoundException, IOException {
		XMLOutputter outputter = new XMLOutputter();
		outputter.preserveSpace(true);
		FileOutputStream output = new FileOutputStream(Constants.CATALOG);
		outputter.output(catalogDoc, output);
		output.close();
	}
	
	private void fillCatalogTable() {
		catalogTable.removeAll();
		holder = null;
		holder = new Vector<Element>();
		count = 0;
		Element root = catalogDoc.getRootElement();
		getShell().setCursor(new Cursor(getShell().getDisplay(),SWT.CURSOR_WAIT));
		recurseCatalog(root);		
		getShell().setCursor(new Cursor(getShell().getDisplay(),SWT.CURSOR_ARROW));
	}
	
	private void recurseCatalog(Element e) {
		List<Element> entries = e.getChildren();
		Iterator<Element> d = entries.iterator();
		while (d.hasNext()) {
			Element entry = d.next();
			String type = entry.getName();
			if (type.equals("public")) { 
				String[] content = {"PUBLIC",  
						entry.getAttributeValue("publicId", ""),  
						entry.getAttributeValue("uri","")};  
				TableItem item = new TableItem(catalogTable, SWT.NONE);
				item.setText(content);
				holder.add(count++,entry);
			} else if (type.equals("system")) { 
				String[] content = {"SYSTEM",  
						entry.getAttributeValue("systemId", ""),  
						entry.getAttributeValue("uri","")};  
				TableItem item = new TableItem(catalogTable, SWT.NONE);
				item.setText(content);
				holder.add(count++,entry);
			} else if (type.equals("uri")) { 
				String[] content = {"URI",  
						entry.getAttributeValue("name", ""),  
						entry.getAttributeValue("uri","")};  
				TableItem item = new TableItem(catalogTable, SWT.NONE);
				item.setText(content);
				holder.add(count++,entry);
			} else if (type.equals("nextCatalog")) { 
				String[] content = {"nextCatalog","", entry.getAttributeValue("catalog")};   
				TableItem item = new TableItem(catalogTable, SWT.NONE);
				item.setText(content);
				holder.add(count++,entry);
			}
			recurseCatalog(entry);
		}
	}
	
	private void editCatalogEntry() {
		if (catalogTable.getSelectionIndices().length == 0) {
			MessageBox box = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.OK);
			box.setMessage("Select a catalog entry."); 
			box.open();			
			return;
		}
		int index = catalogTable.getSelectionIndices()[0];
		Element e = holder.get(index);
		if (!e.getName().equals("nextCatalog")) { 
			EntryConfigurationDialog config = new EntryConfigurationDialog(getShell(),e);
			config.show();
			if ( !config.wasCancelled()) {
				try {
					saveCatalog();
					fillCatalogTable();
				} catch (Exception e1) {
					e1.printStackTrace();
					MessageBox box = new MessageBox(getShell(),SWT.ICON_ERROR|SWT.OK);
					box.setMessage("There was an error saving changes.");
					box.open();
				}
			}	
		} else {
			FileDialog fd = new FileDialog(getShell(),SWT.OPEN);
			fd.setOverwrite(true);
			String[] names = {"XML Files [*.xml]","All Files [*.*]"};  
			String[] extensions = {"*.xml", "*.*"};  
			fd.setFilterNames(names);
			fd.setFilterExtensions(extensions);
			try {
				File cname = new File(FileUtils.getAbsolutePath(Constants.CATALOG,e.getAttributeValue("catalog"))); 
				fd.setFileName(cname.getName());
				fd.setFilterPath(cname.getParent());
			} catch (IOException e2) {
				// do nothing
				e2.printStackTrace();
			}
			String name = fd.open();
			if ( name != null) {
				try {
					e.setAttribute("catalog", FileUtils.getRelativePath(Constants.CATALOG, name)); 
					saveCatalog();
					fillCatalogTable();
				} catch (Exception e1) {
					e1.printStackTrace();
					MessageBox box = new MessageBox(getShell(),SWT.ICON_ERROR|SWT.OK);
					box.setMessage("There was an error saving catalog.");
					box.open();
				}
			}
		}
	}
	
	private void deleteCatalogEntry() {
		if (catalogTable.getSelectionIndices().length == 0) {
			MessageBox box = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.OK);
			box.setMessage("Select a catalog entry."); 
			box.open();			
			return;
		}
		MessageBox box = new MessageBox(getShell(),SWT.ICON_QUESTION|SWT.YES|SWT.NO);
		box.setMessage("Remove selected entry?"); 
		if ( box.open() == SWT.NO) {
			return;
		}
		int index = catalogTable.getSelectionIndices()[0];
		Element e = holder.get(index);
		remove(catalogDoc.getRootElement(),e);
		try {
			saveCatalog();
			fillCatalogTable();
		} catch (Exception e1) {
			e1.printStackTrace();
			MessageBox ebox = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
			ebox.setMessage("There was an error saving catalog."); 
			ebox.open();
		}
				
	}
	
	private void remove(Element main, Element e) {
		try {
			main.removeChild(e);
		} catch (Exception e1) {
			List<Element> content = main.getChildren();
			Iterator<Element> i = content.iterator();
			while (i.hasNext()) {
				Element child = i.next(); 
				remove(child, e);
			}
		}

	}
	
	private void addCatalogEntry() {		
		EntryTypeDialog entryType = new EntryTypeDialog(getShell());
		entryType.show();
		if ( !entryType.wasCancelled()) {
			int type = entryType.getType();
			String name = ""; 
			switch (type) {
			case EntryTypeDialog.PUBLIC:
				name = "public"; 
				break;
			case EntryTypeDialog.SYSTEM:
				name = "system"; 
				break;
			case EntryTypeDialog.URI:
				name = "uri"; 
				break;
			case EntryTypeDialog.nextCatalog:
				addCatalogue();
				return;
			}
			Element e = new Element(name);
			EntryConfigurationDialog config = new EntryConfigurationDialog(getShell(),e);
			config.show();
			if ( !config.wasCancelled()) {
				catalogDoc.getRootElement().addContent(e);
				catalogDoc.getRootElement().addContent("\n"); 
				try {
					saveCatalog();
					fillCatalogTable();
				} catch (Exception e1) {
					e1.printStackTrace();
					MessageBox box = new MessageBox(getShell(),SWT.ICON_ERROR|SWT.OK);
					box.setMessage("There was an error saving catalog.");
					box.open();
				}
			}
		}
	}
	
	private void addCatalogue() {
		FileDialog fd = new FileDialog(getShell(),SWT.OPEN);
		String[] names = {"XML Files [*.xml]", "All Files [*.*]"};  
		String[] extensions = {"*.xml", "*.*"};  
		fd.setFilterNames(names);
		fd.setFilterExtensions(extensions);
		String name = fd.open();
		if ( name != null) {
			try {
				Element e = new Element("nextCatalog"); 
				File catalog = new File(Constants.CATALOG);
				e.setAttribute("catalog", FileUtils.getRelativePath(catalog.getAbsolutePath(), name)); 
				catalogDoc.getRootElement().addContent(e);
				catalogDoc.getRootElement().addContent("\n"); 
				saveCatalog();
				fillCatalogTable();
			} catch (Exception e1) {
				MessageBox box = new MessageBox(getShell(),SWT.ICON_ERROR|SWT.OK);
				box.setMessage(e1.getMessage());
				box.open();
			}
		}
	}

}
