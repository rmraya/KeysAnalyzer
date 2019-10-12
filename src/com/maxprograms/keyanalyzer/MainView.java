package com.maxprograms.keyanalyzer;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.Collator;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.maxprograms.keyanalyzer.models.Analysis;
import com.maxprograms.keyanalyzer.views.AboutBox;
import com.maxprograms.keyanalyzer.views.MapSelectorDialog;
import com.maxprograms.keyanalyzer.views.PreferencesDialog;
import com.maxprograms.utils.Locator;
import com.maxprograms.widgets.CustomBar;
import com.maxprograms.widgets.CustomItem;

public class MainView {

	private Display display;
	private Shell shell;
	private Table table;
	private TableColumn mapColumn;
	private TableColumn dateColumn;
	protected int sortField;
	private Menu systemMenu;
	private boolean isMac;

	public MainView(Display display) {
		this.display = display;
		shell = new Shell(display, SWT.SHELL_TRIM);
		shell.setText("DITA Keys Analyzer");
		shell.setImage(KeyAnalyzer.getResourceManager().getIcon());
		shell.addListener(SWT.Close, new Listener() {

			@Override
			public void handleEvent(Event arg0) {
				Locator.remember(shell, "MainView");
			}
			
		});
		shell.addListener(SWT.Resize, new Listener() {

			@Override
			public void handleEvent(Event arg0) {
				fixColumns();
			}
		});
		
		GridLayout shellLayout = new GridLayout();
		shellLayout.marginWidth = 0;
		shellLayout.marginHeight = 0;
		shell.setLayout(shellLayout);
	
		systemMenu = display.getSystemMenu();
		
		if (systemMenu != null && System.getProperty("os.name").startsWith("Mac")) {
			
			isMac = true;
			
			MenuItem sysItem = getItem(systemMenu, SWT.ID_ABOUT);
			sysItem.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					AboutBox box = new AboutBox(shell, SWT.DIALOG_TRIM);
					box.show();
				}
			});
			sysItem = getItem(systemMenu, SWT.ID_QUIT);
			sysItem.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					shell.close();
				}
			});
			sysItem = getItem(systemMenu, SWT.ID_PREFERENCES);
			sysItem.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					PreferencesDialog dialog = new PreferencesDialog(shell, SWT.CLOSE|SWT.RESIZE);
					dialog.show();
				}
			});
		}
	
		Menu bar = display.getMenuBar();
		if (bar == null) {
			bar = new Menu(shell, SWT.BAR);
			shell.setMenuBar(bar);
		}
		createMenu(bar);
	
		CustomBar customBar = new CustomBar(shell, SWT.NONE);
		customBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		CustomItem openItem = customBar.addItem(SWT.NONE);
		openItem.setText("Analyze Map");
		openItem.setImage(KeyAnalyzer.getResourceManager().getPaper());
		openItem.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent arg0) {
				// do nothing				
			}
			
			@Override
			public void mouseDown(MouseEvent arg0) {
				openMap();
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
				// do nothing				
			}
		});
		
		CustomItem preferences = customBar.addItem(SWT.PUSH);
		preferences.setText("XML Catalog");
		preferences.setImage(KeyAnalyzer.getResourceManager().getGear());
		preferences.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent arg0) {
				// do nothing				
			}
			
			@Override
			public void mouseDown(MouseEvent arg0) {
				PreferencesDialog dialog = new PreferencesDialog(shell, SWT.CLOSE|SWT.RESIZE);
				dialog.show();
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
				// do nothing				
			}
		});
		
		customBar.addFiller();
		
		CustomItem helpItem = customBar.addItem(SWT.PUSH);
		helpItem.setText("User Guide");
		helpItem.setImage(KeyAnalyzer.getResourceManager().getHelp());
		helpItem.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent arg0) {
				// do nothing
				
			}
			
			@Override
			public void mouseDown(MouseEvent arg0) {
				try {
					Program.launch(new File("docs/keyanalyzer.pdf").toURI().toURL().toString());
				} catch (MalformedURLException e) {
					e.printStackTrace();
					MessageBox box = new MessageBox(shell, SWT.ICON_WARNING|SWT.OK);
					box.setMessage("There was an error opening help file.");
					box.open();					
				}
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
				// do nothing				
			}
		});
		
		table = new Table(shell, SWT.H_SCROLL|SWT.V_SCROLL|SWT.SINGLE|SWT.FULL_SELECTION);
		GridData tableData = new GridData(GridData.FILL_BOTH);
		table.setLayoutData(tableData);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.setFocus();
		if (isMac) {
			table.setBackgroundImage(KeyAnalyzer.getResourceManager().getMacBackground());
		} else {
			table.setBackgroundImage(KeyAnalyzer.getResourceManager().getBackground());
		}
		table.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent arg0) {
				// do nothing
			}
			
			@Override
			public void mouseDown(MouseEvent arg0) {
				// do nothing				
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
				if (table.getSelectionCount() == 1) {
					openMap();
				}
			}
		});
		mapColumn = new TableColumn(table, SWT.NONE);
		mapColumn.setText("DITA Map");
		
		dateColumn = new TableColumn(table, SWT.NONE);
		dateColumn.setText("Analyzed");
		
		Listener sortListener = new Listener() {
			public void handleEvent(Event e) {
				if (table.getSortDirection() == SWT.UP) {
					table.setSortDirection(SWT.DOWN);
				} else {
					table.setSortDirection(SWT.UP);
				}
				TableColumn column = (TableColumn) e.widget;
				table.setSortColumn(column);
				if (column == mapColumn) sortField = 0;
				if (column == dateColumn) sortField = 1;
				loadAnalysis();
			}
		};
		
		mapColumn.addListener(SWT.Selection, sortListener);
		dateColumn.addListener(SWT.Selection, sortListener);
		
		table.setSortColumn(mapColumn);
		table.setSortDirection(SWT.UP);
	
		loadAnalysis();
	}

	private void createMenu(Menu bar) {
		MenuItem file = new MenuItem(bar, SWT.CASCADE);
		file.setText("&File");
		Menu fileMenu = new Menu(file);
		file.setMenu(fileMenu);
		
		MenuItem publish = new MenuItem(fileMenu, SWT.PUSH);
		publish.setText("Analyze Map");
		publish.setImage(KeyAnalyzer.getResourceManager().getPaper());
		publish.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				openMap();
			}
		
		});
		
		if (!isMac) {
			
			new MenuItem(fileMenu, SWT.SEPARATOR);
			
			MenuItem close = new MenuItem(fileMenu, SWT.PUSH);
			if (System.getProperty("file.separator").equals("\\")) {
				close.setText("Exit\tAlt + F4");
				close.setAccelerator(SWT.ALT|SWT.F4);
			} else {
				close.setText("Quit\tCtrl + Q");
				close.setAccelerator(SWT.CTRL|'Q');
			}
			close.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					shell.close();
				}
			});
		}
		
		MenuItem settings = new MenuItem(bar, SWT.CASCADE);
		settings.setText("&Settings");
		Menu settingsMenu = new Menu(settings);
		settings.setMenu(settingsMenu);

		MenuItem preferences = new MenuItem(settingsMenu, SWT.PUSH);
		preferences.setText("XML Catalog");
		preferences.setImage(KeyAnalyzer.getResourceManager().getGear());
		preferences.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				PreferencesDialog dialog = new PreferencesDialog(shell, SWT.CLOSE|SWT.RESIZE);
				dialog.show();
			}
		});	
		

		MenuItem help = new MenuItem(bar, SWT.CASCADE);
		help.setText("&Help");
		Menu helpMenu = new Menu(help);
		help.setMenu(helpMenu);
		
		MenuItem helpItem = new MenuItem(helpMenu, SWT.PUSH);
		helpItem.setText("KeyAnalyzer User Guide\tF1");
		helpItem.setAccelerator(SWT.F1);
		helpItem.setImage(KeyAnalyzer.getResourceManager().getHelp());
		helpItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				try {
					Program.launch(new File("docs/keyanalyzer.pdf").toURI().toURL().toString());
				} catch (MalformedURLException e) {
					e.printStackTrace();
					MessageBox box = new MessageBox(shell, SWT.ICON_WARNING|SWT.OK);
					box.setMessage("There was an error opening help file.");
					box.open();					
				}				
			}
		});
	
		new MenuItem(helpMenu, SWT.SEPARATOR);
		
		MenuItem updatesItem = new MenuItem(helpMenu, SWT.PUSH);
		updatesItem.setText("Check for Updates");
		updatesItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				checkUpdates(false);				
			}
		});
	
		MenuItem releaseHistory = new MenuItem(helpMenu, SWT.PUSH);
		releaseHistory.setText("KeyAnalyzer Release History");
		releaseHistory.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				Program.launch("http://www.maxprograms.com/products/keyanalyzerlog.html");			
			}
		});
			
		if (!isMac) {
			new MenuItem(helpMenu, SWT.SEPARATOR);
			
			MenuItem aboutItem = new MenuItem(helpMenu, SWT.PUSH);
			aboutItem.setText("About KeyAnalyzer");
			aboutItem.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					AboutBox box = new AboutBox(shell, SWT.DIALOG_TRIM);
					box.show();
				}
			});			
		}
		
	}

	

	protected void openMap() {
		MapSelectorDialog dialog = new MapSelectorDialog(shell, SWT.DIALOG_TRIM);
		if (table.getSelectionCount() == 1) {
			dialog.setAnalysis((Analysis)table.getSelection()[0].getData("analysis"));
		}
		dialog.show();
	}

	public void show() {
		Locator.position(shell, "MainView"); //$NON-NLS-1$
		shell.open();
		fixColumns();
		display.asyncExec(new Runnable() {
			public void run() {
				checkUpdates(true);
			}
		});
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	protected void checkUpdates(boolean silent) {
		try {
			URL url = new URL("http://www.maxprograms.com/keyanalyzer");  
			URLConnection connection = url.openConnection();
			connection.setConnectTimeout(10000);
			InputStream input = connection.getInputStream();
			int available = input.available();
			byte[] array = new byte[available];
			input.read(array);
			input.close();
			String version = new String(array).trim();
			if (!version.equals(Constants.VERSION  + " (" + Constants.BUILD + ")")) { 
				MessageBox box = new MessageBox(shell,SWT.ICON_QUESTION|SWT.YES|SWT.NO);
				MessageFormat mf = new MessageFormat("Installed version is: {0}\n" +  
						"Available version is: {1}\n" +  
						"\n" + 
						"Visit download site?");  
				Object[] args = {Constants.VERSION + " (" + Constants.BUILD + ")",version}; 
				box.setMessage(mf.format(args)); 
				if (box.open() == SWT.YES) {
					Program.launch("http://www.maxprograms.com/downloads/"); 
				}
			} else {
				if (!silent) {
					MessageBox box = new MessageBox(shell,SWT.ICON_INFORMATION|SWT.OK);
					box.setMessage("No updates available.");  
					box.open();   
				}
			}
		} catch (Exception e) {
			if (!silent) {
				MessageBox box = new MessageBox(shell,SWT.ICON_WARNING|SWT.OK);
				box.setMessage("Unable to check for updates.");  
				box.open();   
			}
		}
	}

	static MenuItem getItem(Menu menu, int id) {
		MenuItem[] items = menu.getItems();
		for (int i = 0; i < items.length; i++) {
			if (items[i].getID() == id) return items[i];
		}
		return null;
	}

	public void loadAnalysis() {
		Cursor arrow = shell.getCursor();
		Cursor wait = new Cursor(display, SWT.CURSOR_WAIT);
		shell.setCursor(wait);
		Vector<Analysis> list = KeyAnalyzer.getController().getAnalysis();
		
		Analysis[] array = (Analysis[]) list.toArray(new Analysis[list.size()]);
		final Collator collator = Collator.getInstance(Locale.getDefault());
		Arrays.sort(array, new Comparator<Analysis>() {

			@Override
			public int compare(Analysis o1, Analysis o2) {
				if (table.getSortDirection() == SWT.UP) {
					switch (sortField) {
					case 0:
						return collator.compare(o1.getMapFile().toLowerCase(Locale.getDefault()), o2.getMapFile().toLowerCase(Locale.getDefault()));
					case 1:
						return o1.getDate().compareTo(o2.getDate());					
					}
				}
				switch (sortField) {
				case 0:
					return collator.compare(o2.getMapFile().toLowerCase(Locale.getDefault()), o1.getMapFile().toLowerCase(Locale.getDefault()));
				case 1:
					return o2.getDate().compareTo(o1.getDate());					
				}
				return 0;
			}
		});
		
		table.removeAll();
		Iterator<Analysis> it = list.iterator();
		SimpleDateFormat dformat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

		while (it.hasNext()) {
			Analysis a = it.next();
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(new String[]{a.getMapFile(), dformat.format(a.getDate())});
			item.setData("analysis", a);
		}
		shell.setCursor(arrow);
		wait.dispose();
	}
	
	protected void fixColumns() {
		if (table != null) {
			Rectangle area = table.getClientArea();
			mapColumn.setWidth(area.width * 66 / 100);
			int vbar = 0;
			ScrollBar vscroll = table.getVerticalBar();
			if (vscroll != null && vscroll.isVisible()) {
				vbar = vscroll.getSize().x;
			}
			dateColumn.setWidth(area.width - mapColumn.getWidth() - vbar);	
		}
	}
}
