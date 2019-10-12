package com.maxprograms.keysanalyzer.controllers;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

public class Scope {

	private Set<String> names;
	private List<Scope> children;
	private Hashtable<String, Key> keys;
	
	public Scope(String name) {
		names = new TreeSet<String>();
		String[] parts = name.split("\\s"); //$NON-NLS-1$
		for (int i=0 ; i<parts.length ; i++) {
			names.add(parts[i]);
		}
		children = new Vector<Scope>();
		keys = new Hashtable<String, Key>();
	}
	
	public void addScope(Scope scope) {
		children.add(scope);
	}
	
	public boolean addKey(Key key) {
		if (!keys.containsKey(key.getName())) {
			keys.put(key.getName(), key);
			return true;
		} 
		return false;
	}
	
	public Key getKey(String key) {
		if (keys.containsKey(key)) {
			Key k = keys.get(key);
			if (k.getHref() != null) {
				return k;
			} 
			return getKey(k.getKeyref());			
		}
		if (key.indexOf('.') != -1) {
			String scope = key.substring(0, key.indexOf('.'));
			key = key.substring(scope.length() + 1);
			if (is(scope)) {
				if (keys.containsKey(key)) {
					return keys.get(key);
				} 
			} else {
				Iterator<Scope> it = children.iterator();
				while (it.hasNext()) {
					Scope child = it.next();
					if (child.is(scope)) {
						return child.getKey(key);
					}
				}
			}
		}
		return null;
	}

	private boolean is(String name) {
		return names.contains(name);
	}
	
	public Hashtable<String, Key> getKeys() {
		Hashtable<String, Key> result = new Hashtable<String, Key>();
		Iterator<String> it = names.iterator();
		while (it.hasNext()) {
			String name = it.next();
			String prefix = ""; //$NON-NLS-1$
			if (!name.equals("")) { //$NON-NLS-1$
				prefix = name + "."; //$NON-NLS-1$
			}
			Set<String> keySet = keys.keySet();
			Iterator<String> kit = keySet.iterator();
			while (kit.hasNext()) {
				String key = kit.next();
				result.put(prefix + key, keys.get(key));
			}
			Iterator<Scope> sc = children.iterator();
			while (sc.hasNext()) {
				Hashtable<String, Key> table = sc.next().getKeys();
				Set<String> set = table.keySet();
				Iterator<String> st = set.iterator();
				while (st.hasNext()) {
					String s = st.next();
					result.put(prefix + s, table.get(s));
				}
			}
		}
		return result;
	}
}
