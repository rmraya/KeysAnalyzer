package com.maxprograms.keyanalyzer.controllers;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.maxprograms.xml.Attribute;
import com.maxprograms.xml.Catalog;
import com.maxprograms.xml.Document;
import com.maxprograms.xml.Element;
import com.maxprograms.xml.SAXBuilder;

public class ScopeBuilder {

	private Scope rootScope;
	private SAXBuilder builder;
	private Scope currentScope;
	private Set<String> recursed;
	private static Hashtable<String, Set<String>> excludeTable;
	private static Hashtable<String, Set<String>> includeTable;
	private static boolean filterAttributes;

	public Scope buildScope(String inputFile, String ditavalFile, Catalog catalog, ILogger logger) throws SAXException, IOException, CancelException, ParserConfigurationException {
		
		if (ditavalFile != null) {
			parseDitaVal(ditavalFile, catalog);
		}
		
		recursed = new TreeSet<String>();
		
		builder = new SAXBuilder();
		builder.setEntityResolver(catalog);
		Document doc = builder.build(inputFile);
		Element root = doc.getRootElement();
		
		currentScope = new Scope(root.getAttributeValue("keyscope", "")); //$NON-NLS-1$ //$NON-NLS-2$
		rootScope = currentScope;
		if (logger != null) {
			if (logger.isCancelled()) {
				throw new CancelException("User cancelled.");
			}
			logger.log(inputFile);
		}
		recurse(root, inputFile, logger);
		
		return rootScope;
	}
	
	private void recurse(Element e, String parentFile, ILogger logger) throws CancelException {
		
		if (filterOut(e)) {
			return;
		}

		if (!e.getAttributeValue("format", "dita").startsWith("dita")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			if (e.getAttributeValue("keys","").equals("")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				return;
			}
		}
		
		if (e.getAttributeValue("scope","local").equals("external")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return;
		}
		
		String scope = e.getAttributeValue("keyscope", ""); //$NON-NLS-1$ //$NON-NLS-2$
		if (!scope.equals("")) { //$NON-NLS-1$
			Scope c = new Scope(scope);
			currentScope.addScope(c);
			currentScope = c;
		}
		
		String href = e.getAttributeValue("href","");   //$NON-NLS-1$ //$NON-NLS-2$
				
		String path = ""; //$NON-NLS-1$
		if (!href.equals("")) { //$NON-NLS-1$
			try {
				path = FileUtils.getAbsolutePath(parentFile, href);
				if (!recursed.contains(path)) {
					File f = new File(path);
					if (f.exists() && f.isFile()) {
						String format = e.getAttributeValue("format", "dita"); //$NON-NLS-1$ //$NON-NLS-2$
						if (format.startsWith("dita") ) { //$NON-NLS-1$
							if (!DitaParser.ditaClass(e, "topic/image")) { //$NON-NLS-1$
								Element root = builder.build(f).getRootElement();
								Scope old = currentScope;
								if (logger != null) {
									if (logger.isCancelled()) {
										throw new CancelException("User cancelled.");
									}
									logger.log(path);
								}
								recursed.add(path);
								recurse(root, path, logger);
								currentScope = old;
							}
						}
					}
				}
			} catch (IOException | SAXException | ParserConfigurationException e1) {
				// skipped images and broken links
			}	
		}
		
		String val = e.getAttributeValue("keys","");   //$NON-NLS-1$ //$NON-NLS-2$
		
		if (!val.equals("")) {  //$NON-NLS-1$
			String[] keys = val.split("\\s");  //$NON-NLS-1$
			Element topicmeta = e.getChild("topicmeta"); //$NON-NLS-1$
			String keyref = e.getAttributeValue("keyref", ""); //$NON-NLS-1$ //$NON-NLS-2$
			for (int i = 0 ; i<keys.length ; i++) {
				String key = keys[i];
				if (!keyref.equals("")) { //$NON-NLS-1$
					if (!currentScope.addKey(new Key(key, keyref, parentFile))) {
						MessageFormat mf = new MessageFormat("Duplicate key definition: {0} -> {1}"); //$NON-NLS-1$
						if (logger != null) {
							logger.logError(mf.format(new Object[]{key, keyref}));
						} else {
							System.err.println(mf.format(new Object[]{key, keyref}));
						}
					}
				} else {
					if (href.equals("")) { //$NON-NLS-1$
						if (!currentScope.addKey(new Key(key, parentFile, topicmeta, parentFile))) {
							MessageFormat mf = new MessageFormat("Duplicate key definition: {0}"); //$NON-NLS-1$
							if (logger != null) {
								logger.logError(mf.format(new Object[]{key}));
							} else {
								System.err.println(mf.format(new Object[]{key}));
							}
						}
					} else {
						try {
							path = FileUtils.getAbsolutePath(parentFile, href);
							File f = new File(path);
							if (f.exists() && f.isFile()) {
								if (!currentScope.addKey(new Key(key, path, topicmeta, parentFile))) {
									MessageFormat mf = new MessageFormat("Duplicate key definition: {0} -> {1}"); //$NON-NLS-1$
									if (logger != null) {
										logger.logError(mf.format(new Object[]{key, path}));
									} else {
										System.err.println(mf.format(new Object[]{key, path}));
									}
								}
							}
						} catch (IOException ioe) {
							// ignore files that can't be parsed
						}
					}
				}
			}
		} 
		List<Element> children = e.getChildren();
		Iterator<Element> it = children.iterator();
		while (it.hasNext()) {
			Scope old = currentScope;
			recurse(it.next(), parentFile, logger);
			currentScope = old;
		}
	}
	
	private static void parseDitaVal(String ditaval, Catalog catalog) throws SAXException, IOException, ParserConfigurationException {
		SAXBuilder bder = new SAXBuilder();
		bder.setEntityResolver(catalog);
		Document doc = bder.build(ditaval);
		Element root = doc.getRootElement();
		if (root.getName().equals("val")) {  //$NON-NLS-1$
			List<Element> props = root.getChildren("prop");  //$NON-NLS-1$
			Iterator<Element> it = props.iterator();
			excludeTable = new Hashtable<String, Set<String>>();
			includeTable = new Hashtable<String, Set<String>>();
			while (it.hasNext()) {
				Element prop = it.next();
				if (prop.getAttributeValue("action", "include").equals("exclude")) {    //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
					String att = prop.getAttributeValue("att", "");   //$NON-NLS-1$ //$NON-NLS-2$
					String val = prop.getAttributeValue("val", "");   //$NON-NLS-1$ //$NON-NLS-2$
					if (!att.equals("")) {  //$NON-NLS-1$
						Set<String> set = excludeTable.get(att);
						if (set == null) {
							set = new HashSet<String>();
						}
						if (!val.equals("")) {  //$NON-NLS-1$
							set.add(val); 
						}
						excludeTable.put(att, set);
					}
				}
				if (prop.getAttributeValue("action", "include").equals("include")) {    //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
					String att = prop.getAttributeValue("att", "");   //$NON-NLS-1$ //$NON-NLS-2$
					String val = prop.getAttributeValue("val", "");   //$NON-NLS-1$ //$NON-NLS-2$
					if (!att.equals("") && !val.equals("")) {   //$NON-NLS-1$ //$NON-NLS-2$
						Set<String> set = includeTable.get(att);
						if (set == null) {
							set = new HashSet<String>();
						}
						set.add(val); 
						includeTable.put(att, set);
					}
				}
			}
			filterAttributes = true;
		}
	}
	
	private static boolean filterOut(Element e) {
		if (filterAttributes) {
			List<Attribute> atts = e.getAttributes();
			Iterator<Attribute> it = atts.iterator();
			while (it.hasNext()) {
				Attribute a = it.next();
				if (excludeTable.containsKey(a.getName())) {
					Set<String> forbidden = excludeTable.get(a.getName()); 
					if (forbidden.size() == 0) {
						if (includeTable.containsKey(a.getName())) {
							Set<String> allowed = includeTable.get(a.getName());
							StringTokenizer tokenizer = new StringTokenizer(a.getValue());
							while (tokenizer.hasMoreTokens()) {
								String token = tokenizer.nextToken();
								if (allowed.contains(token)) {
									return false;
								}
							}
						}
					}
					StringTokenizer tokenizer = new StringTokenizer(a.getValue());
					Vector<String> tokens = new Vector<String>();
					while (tokenizer.hasMoreTokens()) {
						tokens.add(tokenizer.nextToken());
					}
					if (forbidden.containsAll(tokens)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
}
