package com.maxprograms.keysanalyzer.controllers;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;

import com.maxprograms.keysanalyzer.models.Analysis;
import com.maxprograms.utils.Preferences;

public class Controller {

	private DB mapdb;
	private HTreeMap<String, Analysis> hashmap;

	public Controller() throws IOException {
		File out = new File(Preferences.getPreferencesDir(), "Analysis");
		try {
			mapdb =  DBMaker.newFileDB(out).closeOnJvmShutdown().asyncWriteEnable().make();
		} catch (IOError ex) {
			if (out.exists()) {
				try {
					out.delete();
					File p = new File(Preferences.getPreferencesDir(), "Analysis" + ".p"); //$NON-NLS-1$
					if (p.exists()) {
						p.delete();
					}
					File t = new File(Preferences.getPreferencesDir(), "Analysis" + ".t"); //$NON-NLS-1$
					if (t.exists()) {
						t.delete();
					}
					mapdb =  DBMaker.newFileDB(out).closeOnJvmShutdown().asyncWriteEnable().make();
				} catch (IOError ex2) {
					throw new IOException(ex2.getMessage());
				}
			} else {
				throw new IOException(ex.getMessage());
			}			
		}
		hashmap = mapdb.getHashMap("analysis"); //$NON-NLS-1$
	}
	
	public void close() {
		if (mapdb != null) {
			mapdb.commit();
			mapdb.compact();
			mapdb.close();
		}
	}

	public Vector<Analysis> getAnalysis() {
		Vector<Analysis> result = new Vector<Analysis>();
		Set<String> set = hashmap.keySet();
		Iterator<String> it = set.iterator();
		while (it.hasNext()) {
			Analysis a = hashmap.get(it.next());
			File map = new File(a.getMapFile());
			if (map.exists()) {
				result.add(a);
			} else {
				removeAnalysis(a);
			}
		}
		return result;
	}

	public  void addAnalysis(Analysis data) {
		data.setLastDate(new Date());
		hashmap.put(data.getMapFile(), data);
		mapdb.commit();
	}

	public void removeAnalysis(Analysis p) {
		hashmap.remove(p.getMapFile());
		mapdb.commit();
	}

}
