/*********************************************************************** 

Copyright (c) 2016-2019 - Maxprograms,  http://www.maxprograms.com/

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
package com.maxprograms.utils;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.Hashtable;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;

public class Preferences {

	private DB mapdb;
	private HTreeMap<String, Hashtable<String, String>> hashmap;
	private static Preferences instance;
	private static Hashtable<String, Preferences> instances;

	public static Preferences getInstance(String file) throws IOException {
		if (instances == null) {
			instances = new Hashtable<String, Preferences>();
		}
		instance = instances.get(file);
		if (instance == null) {
			instance = new Preferences(file);
			instances.put(file, instance);
		}
		return instance;
	}

	private Preferences(String file) throws IOException {
		File out = new File(getPreferencesDir(), file);
		try {
			mapdb = DBMaker.newFileDB(out).closeOnJvmShutdown().asyncWriteEnable().make();
		} catch (IOError ex) {
			if (out.exists()) {
				try {
					out.delete();
					File p = new File(getPreferencesDir(), file + ".p"); //$NON-NLS-1$
					if (p.exists()) {
						p.delete();
					}
					File t = new File(getPreferencesDir(), file + ".t"); //$NON-NLS-1$
					if (t.exists()) {
						t.delete();
					}
					mapdb = DBMaker.newFileDB(out).closeOnJvmShutdown().asyncWriteEnable().make();
				} catch (IOError ex2) {
					throw new IOException(ex2.getMessage());
				}
			} else {
				throw new IOException(ex.getMessage());
			}
		}
		hashmap = mapdb.getHashMap("preferences"); //$NON-NLS-1$
	}

	public synchronized static File getPreferencesDir() throws IOException {
		String directory;
		if (File.separator.equals("\\")) { //$NON-NLS-1$ //$NON-NLS-2$
			// Windows
			directory = System.getenv("AppData") + "\\Maxprograms\\KeysAnalyzer\\"; //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			String os = System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$ 
			if (os.startsWith("mac")) { //$NON-NLS-1$ 
				// Mac
				directory = System.getProperty("user.home") + "/Library/Application Support/Maxprograms/KeysAnalyzer/"; //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				// Linux
				directory = System.getProperty("user.home") + "/.maxprograms/KeysAnalyzer/"; //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		File dir = new File(directory);
		if (!dir.exists()) {
			Files.createDirectories(dir.toPath());
		}
		return dir;
	}

	public synchronized void save(String group, String name, String value) {
		Hashtable<String, String> g = hashmap.get(group);
		if (g == null) {
			g = new Hashtable<String, String>();
		}
		g.put(name, value);
		hashmap.put(group, g);
		mapdb.commit();
	}

	public String get(String group, String name, String defaultValue) {
		Hashtable<String, String> g = hashmap.get(group);
		if (g == null) {
			return defaultValue;
		}
		String value = g.get(name);
		if (value == null) {
			return defaultValue;
		}
		return value;
	}

	public synchronized void save(String group, Hashtable<String, String> table) {
		Hashtable<String, String> g = hashmap.get(group);
		if (g != null) {
			Enumeration<String> keys = table.keys();
			while (keys.hasMoreElements()) {
				String key = keys.nextElement();
				g.put(key, table.get(key));
			}
			hashmap.put(group, g);
		} else {
			hashmap.put(group, table);
		}
		mapdb.commit();
	}

	public Hashtable<String, String> get(String group) {
		Hashtable<String, String> g = hashmap.get(group);
		if (g == null) {
			g = new Hashtable<String, String>();
		}
		return g;
	}

	public synchronized void remove(String group) {
		Hashtable<String, String> g = hashmap.get(group);
		if (g != null) {
			hashmap.remove(group);
			mapdb.commit();
		}
	}

	public void close() {
		mapdb.commit();
		mapdb.close();
	}
}
