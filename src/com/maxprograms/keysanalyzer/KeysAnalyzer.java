package com.maxprograms.keysanalyzer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import com.maxprograms.keysanalyzer.controllers.Controller;
import com.maxprograms.keysanalyzer.views.resources.ResourceManager;
import com.maxprograms.utils.Preferences;

public class KeysAnalyzer {

	private static File lock;
	private static FileChannel channel;
	private static FileLock flock;
	private static FileOutputStream lockStream;
	private static Display display;
	private static ResourceManager resourceManager;
	private static MainView main;
	private static Controller controller;
	private static String catalog;

	public static void main(String[] args) {
		Display.setAppName("KeysAnalyzer");
		Display.setAppVersion(Constants.VERSION);
		display = Display.getDefault();

		resourceManager = new ResourceManager(display);
		try {

			checkLock();
			lock();

			Preferences preferences = Preferences.getInstance(Constants.PREFERENCES);
			catalog = preferences.get("KeysAnalyzer", "catalog", "");
			if (catalog.isEmpty()) {
				File cat = new File("catalog/catalog.xml");
				catalog = cat.getAbsolutePath();
				preferences.save("KeysAnalyzer", "catalog", catalog);
			}

			controller = new Controller();
			main = new MainView(display);
			main.show();
			if (!display.isDisposed()) {
				display.dispose();
			}
			controller.close();
			unlock();
		} catch (Exception e) {
			try {
				File log = new File(Preferences.getPreferencesDir().getParentFile(), "KeysAnalyzer_error.log"); //$NON-NLS-1$
				PrintStream stream = new PrintStream(log);
				e.printStackTrace(stream);
				stream.close();
			} catch (Exception e2) {
				e.printStackTrace();
			}
		}
	}

	public static String getCatalog() {
		return catalog;
	}

	public static void setCatalog(String value) throws IOException {
		Preferences preferences = Preferences.getInstance(Constants.PREFERENCES);
		preferences.save("KeysAnalyzer", "catalog", value);
		catalog = value;
	}

	private static void checkLock() throws IOException {
		File old = new File(Preferences.getPreferencesDir(), "lock"); //$NON-NLS-1$
		if (old.exists()) {
			try (RandomAccessFile file = new RandomAccessFile(old, "rw")) { //$NON-NLS-1$
				try (FileChannel oldchannel = file.getChannel()) {
					try (FileLock newlock = oldchannel.tryLock()) {
						if (newlock == null) {
							Shell shell = new Shell(display);
							shell.setImage(resourceManager.getIcon());
							MessageBox box = new MessageBox(shell, SWT.ICON_WARNING);
							box.setText("KeysAnalyzer");
							box.setMessage("An instance of this application is already running.");
							box.open();
							display.dispose();
							System.exit(1);
						} else {
							newlock.release();
						}
					}
				}
			}
			Files.delete(Paths.get(old.toURI()));
		}
	}

	private static void unlock() throws IOException {
		flock.release();
		channel.close();
		lockStream.close();
		Files.delete(Paths.get(lock.toURI()));
	}

	private static void lock() throws IOException {
		lock = new File(Preferences.getPreferencesDir(), "lock"); //$NON-NLS-1$
		lockStream = new FileOutputStream(lock);
		Date d = new Date(System.currentTimeMillis());
		lockStream.write(d.toString().getBytes(StandardCharsets.UTF_8));
		channel = lockStream.getChannel();
		flock = channel.lock();
	}

	public static ResourceManager getResourceManager() {
		return resourceManager;
	}

	public static MainView getMainView() {
		return main;
	}

	public static Controller getController() {
		return controller;
	}
}
