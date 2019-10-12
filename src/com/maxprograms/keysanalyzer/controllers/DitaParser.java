package com.maxprograms.keysanalyzer.controllers;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
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

public class DitaParser {

	
	protected class StringArray implements Comparable<StringArray> {
		private String file;
		private String id;

		public StringArray(String file, String id) {
			this.file = file;
			this.id = id;
		}

		@Override
		public int compareTo(StringArray o) {
			if (o.getFile().equals(file) && o.getId().equals(id)) {
				return 0;
			}
			return 1;
		}

		public String getFile() {
			return file;
		}

		public String getId() {
			return id;
		}
	}
	
	private static Set<String> filesMap;
	private static SAXBuilder builder;
	private static Hashtable<String, Set<String>> excludeTable;
	private static Hashtable<String, Set<String>> includeTable;
	private static boolean filterAttributes;
	private Scope rootScope;
	
	private Set<StringArray> searchedConref;
	Hashtable<Key, Set<String>> usedKeys;
	Hashtable<String, Set<String>> usedAs;
	private Set<String> recursed;
	private Set<String> pendingRecurse;
	private static TreeSet<String> topicrefSet;
	private static TreeSet<String> xrefSet;
	private static TreeSet<String> linkSet;
	private static TreeSet<String> topicSet;
	private static TreeSet<String> imageSet;

	public Vector<String> run(Hashtable<String, String> params, ILogger logger) throws IOException, SAXException, ParserConfigurationException, CancelException, URISyntaxException {
		Vector<String> result = new Vector<String>();
		filesMap = new TreeSet<String>();
		searchedConref = new TreeSet<StringArray>();
		usedKeys = new Hashtable<Key, Set<String>>();
		usedAs = new Hashtable<String, Set<String>>();
		recursed = new TreeSet<String>();
		pendingRecurse = new TreeSet<String>();
		
		String inputFile = params.get("source");  //$NON-NLS-1$
		Catalog catalog = new Catalog(params.get("catalogue"));  //$NON-NLS-1$
		String ditaval = params.get("ditaval");  //$NON-NLS-1$

		if (logger != null) {
			if (logger.isCancelled()) {
				throw new CancelException("User cancelled.");
			}
			logger.setStage("Harvesting Keys"); //$NON-NLS-1$
		}

		ScopeBuilder sbuilder = new ScopeBuilder();
		rootScope = sbuilder.buildScope(inputFile, ditaval, catalog, logger);
		if (logger.isCancelled()) {
			throw new CancelException("User cancelled.");
		}
		if (ditaval != null) {
			parseDitaVal(ditaval, catalog);
		}

		filesMap = new TreeSet<String>();
		filesMap.add(inputFile);

		if (logger != null) {
			if (logger.isCancelled()) {
				throw new CancelException("User cancelled.");
			}
			logger.setStage("Building Files List");  //$NON-NLS-1$
		}

		builder = new SAXBuilder();
		builder.setEntityResolver(catalog);
		Document doc = builder.build(inputFile);
		Element root = doc.getRootElement();
		
		recurse(root, inputFile, logger);
		recursed.add(inputFile);

		Iterator<Key> it = usedKeys.keySet().iterator();
		while (it.hasNext()) {
			Key key = it.next();
			String defined = key.getDefined();
			if (!filesMap.contains(defined)) {
				pendingRecurse.add(defined);				
			}
			if (!filesMap.contains(key.getHref())) {
				pendingRecurse.add(key.getHref());
			}
		}
		
		
		int count = 0;
		do {
			Vector<String> files = new Vector<String>();
			files.addAll(pendingRecurse);
			pendingRecurse.clear();
			Iterator<String> st = files.iterator();
			while (st.hasNext()) {
				String file = st.next();
				if (!recursed.contains(file)) {
					if (logger != null) {
						if (logger.isCancelled()) {
							throw new CancelException("User cancelled.");
						}
						logger.log(file);
					}
					try {
						Element e = builder.build(file).getRootElement();
						recurse(e, file, logger);
						if (logger != null) {
							if (logger.isCancelled()) {
								throw new CancelException("User cancelled.");
							}
						}
						recursed.add(file);
						filesMap.add(file);
					} catch (CancelException e) {
						throw e;
					} catch (Exception e) {
						// ignore keys that point to images
					}
				}				
			}
			count++;
		} while (pendingRecurse.size() > 0 && count < 4);
		
		result.addAll(filesMap);
		return result;
	}


	private void recurse(Element e, String parentFile, ILogger logger) throws IOException, SAXException, CancelException, ParserConfigurationException {
		if (filterOut(e)) {
			return;
		}
		
		if (ditaClass(e, "map/topicref") || isTopicref(e.getName())) { //$NON-NLS-1$
			String href = "";  //$NON-NLS-1$
			String keyref = e.getAttributeValue("keyref", ""); //$NON-NLS-1$ //$NON-NLS-2$
			if (!keyref.equals("")) { //$NON-NLS-1$
				Key k = rootScope.getKey(keyref);
				if (k != null) {
					if (!usedKeys.containsKey(k)) {
						usedKeys.put(k, new TreeSet<String>());
					}
					usedKeys.get(k).add(parentFile);
					if (!usedAs.containsKey(keyref)) {
						usedAs.put(keyref, new TreeSet<String>());
					}
					usedAs.get(keyref).add(parentFile);
					href = k.getHref();
				}
				if (k!=null && k.getTopicmeta() != null && e.getContent().size() == 0) {
					e.addContent(k.getTopicmeta());
					return;
				} 								
			} else {
				href = e.getAttributeValue("href", ""); //$NON-NLS-1$ //$NON-NLS-2$
				String format = e.getAttributeValue("format","dita"); //$NON-NLS-1$ //$NON-NLS-2$
				if (!href.equals("") && format.startsWith("dita")) { //$NON-NLS-1$ //$NON-NLS-2$
					href = FileUtils.getAbsolutePath(parentFile, href);
				}
			}
			if (href.startsWith("http:") || href.startsWith("https:") || href.startsWith("ftp:") || href.startsWith("mailto:")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				e.setAttribute("scope", "external"); //$NON-NLS-1$ //$NON-NLS-2$
				return;
			}
			String format = e.getAttributeValue("format", "dita"); //$NON-NLS-1$ //$NON-NLS-2$
			if (!href.equals("") && !format.startsWith("dita")) { //$NON-NLS-1$ //$NON-NLS-2$
				return;
			}
			if (!href.equals("") && !href.equals(parentFile)) { //$NON-NLS-1$
				try {
					Element root = builder.build(href).getRootElement();
					if (logger != null) {
						if (logger.isCancelled()) {
							throw new CancelException("User cancelled.");
						}
						logger.log(href);
					}
					if (!recursed.contains(href)) {
						recurse(root, href, logger);
						filesMap.add(href);
						recursed.add(href);
					}
				} catch (CancelException ex) {
					throw ex;
				} catch (Exception ex) {
					if (logger != null) {
						File f = new File(href);
						if (!f.exists()) {
							MessageFormat mf = new MessageFormat("File \"{0}\" referenced by \"{1}\" does not exist."); //$NON-NLS-1$
							logger.logError(mf.format(new Object[]{href, parentFile}));
						} else {
							if (ex.getMessage() != null) {
								logger.logError(ex.getMessage());
							} else {
								ex.printStackTrace();
							}
						}
					} else {
						ex.printStackTrace();
					}
				}
			}
		} else {
			String conref = e.getAttributeValue("conref", ""); //$NON-NLS-1$ //$NON-NLS-2$
			if (!conref.equals("")) { //$NON-NLS-1$
				if (conref.indexOf("#") != -1) {  //$NON-NLS-1$
					String file = conref.substring(0, conref.indexOf("#"));  //$NON-NLS-1$
					if (file.length() == 0) {
						file = parentFile;
					} else {
						file = FileUtils.getAbsolutePath(parentFile, file);
					}
					String id = conref.substring(conref.indexOf("#") + 1);  //$NON-NLS-1$
					try {
						Element ref = getReferenced(file, id, logger);
						if (ref != null) {
							e.setContent(ref.getContent());
						} else {
							MessageFormat mf = new MessageFormat("@conref not found:  \"{0}\" in file {1}"); //$NON-NLS-1$
							if (logger != null) {
								if (logger != null) {
									if (logger.isCancelled()) {
										throw new CancelException("User cancelled.");
									}
								}
								logger.logError(mf.format(new Object[] {conref, parentFile}));
							} else {
								System.err.println(mf.format(new Object[] {conref, parentFile}));
							}
						}
						return;
					} catch (CancelException ex) {
						throw ex;
					} catch (Exception ex) {
						
						MessageFormat mf = new MessageFormat("Broken @conref \"{0}\" in file {1}"); //$NON-NLS-1$
						if (logger != null) {
							logger.logError(mf.format(new Object[] {conref, parentFile}));
						} else {
							System.err.println(mf.format(new Object[] {conref, parentFile}));
						}
					}
					return;
				} 
				System.err.println("@conref without fragment identifier: " + conref); //$NON-NLS-1$				
			}

			String conkeyref = e.getAttributeValue("conkeyref", ""); //$NON-NLS-1$ //$NON-NLS-2$
			String conaction = e.getAttributeValue("conaction", "");  //$NON-NLS-1$ //$NON-NLS-2$
			if (!conaction.equals("")) { // it's a conref push   //$NON-NLS-1$
				conkeyref = "";   //$NON-NLS-1$
			}
			if (!conkeyref.equals("")) { //$NON-NLS-1$
				String key = conkeyref.substring(0, conkeyref.indexOf("/")); //$NON-NLS-1$
				String id = conkeyref.substring(conkeyref.indexOf("/") + 1); //$NON-NLS-1$
				Key k = rootScope.getKey(key);
				if (k == null) {
					MessageFormat mf = new MessageFormat("Key not defined for @conkeyref: \"{0}\"."); //$NON-NLS-1$
					if (logger != null) {
						logger.logError(mf.format(new Object[]{conkeyref})); 
					} else {
						System.err.println(mf.format(new Object[]{conkeyref}));
					}
					return;
				}
				if (!usedKeys.containsKey(k)) {
					usedKeys.put(k, new TreeSet<String>());
				}
				usedKeys.get(k).add(parentFile);
				if (!usedAs.containsKey(conkeyref)) {
					usedAs.put(conkeyref, new TreeSet<String>());
				}
				usedAs.get(conkeyref).add(parentFile);
				String file = k.getHref();
				if (file == null) {
					MessageFormat mf = new MessageFormat("Key not defined for @conkeyref: \"{0}\"."); //$NON-NLS-1$
					if (logger != null) {
						logger.logError(mf.format(new Object[]{conkeyref})); 
					} else {
						System.err.println(mf.format(new Object[]{conkeyref}));
					}
					return;
				}

				Element ref = getConKeyReferenced(file, id, logger);
				if (ref != null) {
					e.setContent(ref.getContent());
					List<Element> children = e.getChildren();
					Iterator<Element> ie = children.iterator();
					while (ie.hasNext()) {
						recurse(ie.next(), file, logger);
					}
					return;
				} 
				MessageFormat mf = new MessageFormat("Broken @conkeyref \"{0}\" in file {1}"); //$NON-NLS-1$
				if (logger != null) {
					logger.logError(mf.format(new Object[]{conkeyref, parentFile}));
				} else {
					System.err.println(mf.format(new Object[]{conkeyref, parentFile}));
				}
				return;
			}		

			String keyref = e.getAttributeValue("keyref", ""); //$NON-NLS-1$ //$NON-NLS-2$
			if (!keyref.equals("")) { //$NON-NLS-1$
				if (keyref.indexOf("/") == -1) { //$NON-NLS-1$
					Key k = rootScope.getKey(keyref);			
					if (k != null) {
						if (!usedKeys.containsKey(k)) {
							usedKeys.put(k, new TreeSet<String>());
						}
						usedKeys.get(k).add(parentFile);
						if (!usedAs.containsKey(keyref)) {
							usedAs.put(keyref, new TreeSet<String>());
						}
						usedAs.get(keyref).add(parentFile);
						if (k!=null && k.getTopicmeta() != null && e.getContent().size() == 0) {

							// empty element that reuses from <topicmenta>
							Element matched = getMatched(e.getName(), k.getTopicmeta());
							if (matched != null) {
								e.setContent(matched.getContent());
								return;
							}
							if (ditaClass(e, "topic/image") || isImage(e.getName())) {  //$NON-NLS-1$
								Element keyword = getMatched("keyword", k.getTopicmeta()); //$NON-NLS-1$
								if (keyword != null) {
									Element alt = new Element("alt"); //$NON-NLS-1$
									alt.setContent(keyword.getContent());
									e.addContent(alt);
									return;
								}
							}
							if (ditaClass(e, "topic/xref") || ditaClass(e, "topic/link") || isXref(e.getName()) || isLink(e.getName())) { //$NON-NLS-1$ //$NON-NLS-2$
								Element keyword = getMatched("keyword", k.getTopicmeta()); //$NON-NLS-1$
								if (keyword != null) {
									Element alt = new Element("linktext"); //$NON-NLS-1$
									alt.setContent(keyword.getContent());
									e.addContent(alt);
									return;
								}
							}
							Element keyword = getMatched("keyword", k.getTopicmeta()); //$NON-NLS-1$
							if (keyword != null) {
								e.addContent(keyword);
								return;
							}
							return;
						}

						pendingRecurse.add(k.getHref());
						return;
					}
				} else {

					// behaves like a conkeyref
					// locate an element in the file referenced by the key

					String key = keyref.substring(0, keyref.indexOf("/")); //$NON-NLS-1$
					String id = keyref.substring(keyref.indexOf("/") + 1); //$NON-NLS-1$
					Key k = rootScope.getKey(key);
					if (k != null) {
						if (!usedKeys.containsKey(k)) {
							usedKeys.put(k, new TreeSet<String>());
						}
						usedKeys.get(k).add(parentFile);
						if (!usedAs.containsKey(keyref)) {
							usedAs.put(keyref, new TreeSet<String>());
						}
						usedAs.get(keyref).add(parentFile);
						filesMap.add(k.getHref());
						Element ref = getConKeyReferenced(k.getHref(), id, logger);
						if (ref != null) {
							e.setContent(ref.getContent());
							List<Element> children = e.getChildren();
							Iterator<Element> ie = children.iterator();
							while (ie.hasNext()) {
								recurse(ie.next(), k.getHref(), logger);
							}
							return;
						}
					}
				}
				MessageFormat mf = new MessageFormat("Undefined key for @keyref \"{0}\" in file {1}");  //$NON-NLS-1$
				if (logger != null) {
					logger.logError(mf.format(new Object[]{keyref, parentFile}));
				} else {
					System.err.println(mf.format(new Object[]{keyref, parentFile}));
				}
			}

			String href = e.getAttributeValue("href", ""); //$NON-NLS-1$ //$NON-NLS-2$
			if (!href.equals("")) { //$NON-NLS-1$
				if ((ditaClass(e, "topic/image") || isImage(e.getName())) && !"external".equals(e.getAttributeValue("scope"))) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					// check for SVG
					try {
						String path = FileUtils.getAbsolutePath(parentFile, href);
						File f = new File(path);
						Element s = builder.build(f).getRootElement();
						if (s.getName().equals("svg")) { //$NON-NLS-1$
							filesMap.add(path);
						}
					} catch (Exception ex) {
						// ignore
					}
				}
			}
		}
		List<Element> children = e.getChildren();
		Iterator<Element> it = children.iterator();
		while (it.hasNext()) {
			recurse(it.next(), parentFile, logger);
		}
	}
	
	static boolean isImage(String name) {
		if (imageSet == null) { 
			imageSet = new TreeSet<String>();
			imageSet.add("image"); //$NON-NLS-1$
			imageSet.add("glossSymbol"); //$NON-NLS-1$
			imageSet.add("hazardsymbol"); //$NON-NLS-1$
		}
		return imageSet.contains(name);
	}

	static boolean isLink(String name) {
		if (linkSet == null) {
			linkSet = new TreeSet<String>();
			linkSet.add("link"); //$NON-NLS-1$
		}
		return linkSet.contains(name);
	}

	static boolean isXref(String name) {
		if (xrefSet == null) {
			xrefSet = new TreeSet<String>();
			xrefSet.add("xref"); //$NON-NLS-1$
			xrefSet.add("glossAlternateFor"); //$NON-NLS-1$
			xrefSet.add("coderef"); //$NON-NLS-1$
			xrefSet.add("fragref"); //$NON-NLS-1$
			xrefSet.add("synnoteref"); //$NON-NLS-1$
			xrefSet.add("mathmlref"); //$NON-NLS-1$
			xrefSet.add("svgref"); //$NON-NLS-1$
		}
		return xrefSet.contains(name);
	}

	static boolean isTopicref(String name) {
		if (topicrefSet == null) {
			topicrefSet = new TreeSet<String>();
			topicrefSet.add("topicref"); //$NON-NLS-1$
			topicrefSet.add("abbrevlist"); //$NON-NLS-1$
			topicrefSet.add("amendments"); //$NON-NLS-1$
			topicrefSet.add("appendix"); //$NON-NLS-1$
			topicrefSet.add("backmatter"); //$NON-NLS-1$
			topicrefSet.add("bibliolist"); //$NON-NLS-1$
			topicrefSet.add("bookabstract"); //$NON-NLS-1$
			topicrefSet.add("booklist"); //$NON-NLS-1$
			topicrefSet.add("booklists"); //$NON-NLS-1$
			topicrefSet.add("chapter"); //$NON-NLS-1$
			topicrefSet.add("colophon"); //$NON-NLS-1$
			topicrefSet.add("dedication"); //$NON-NLS-1$
			topicrefSet.add("draftintro"); //$NON-NLS-1$
			topicrefSet.add("figurelist"); //$NON-NLS-1$
			topicrefSet.add("frontmatter"); //$NON-NLS-1$
			topicrefSet.add("glossarylist"); //$NON-NLS-1$
			topicrefSet.add("indexlist"); //$NON-NLS-1$
			topicrefSet.add("notices"); //$NON-NLS-1$
			topicrefSet.add("part"); //$NON-NLS-1$
			topicrefSet.add("preface"); //$NON-NLS-1$
			topicrefSet.add("tablelist"); //$NON-NLS-1$
			topicrefSet.add("toc"); //$NON-NLS-1$
			topicrefSet.add("trademarklist"); //$NON-NLS-1$
			topicrefSet.add("anchorref"); //$NON-NLS-1$
			topicrefSet.add("keydef"); //$NON-NLS-1$
			topicrefSet.add("mapref"); //$NON-NLS-1$
			topicrefSet.add("topicgroup"); //$NON-NLS-1$
			topicrefSet.add("topichead"); //$NON-NLS-1$
			topicrefSet.add("topicset"); //$NON-NLS-1$
			topicrefSet.add("topicsetref"); //$NON-NLS-1$
			topicrefSet.add("ditavalref"); //$NON-NLS-1$
			topicrefSet.add("glossref"); //$NON-NLS-1$
			topicrefSet.add("subjectref"); //$NON-NLS-1$
			topicrefSet.add("topicapply"); //$NON-NLS-1$
			topicrefSet.add("topicsubject"); //$NON-NLS-1$
			topicrefSet.add("defaultSubject"); //$NON-NLS-1$
			topicrefSet.add("enumerationdef"); //$NON-NLS-1$
			topicrefSet.add("hasInstance"); //$NON-NLS-1$
			topicrefSet.add("hasKind"); //$NON-NLS-1$
			topicrefSet.add("hasNarrower"); //$NON-NLS-1$
			topicrefSet.add("hasPart"); //$NON-NLS-1$
			topicrefSet.add("hasRelated"); //$NON-NLS-1$
			topicrefSet.add("relatedSubjects"); //$NON-NLS-1$
			topicrefSet.add("subjectdef"); //$NON-NLS-1$
			topicrefSet.add("subjectHead"); //$NON-NLS-1$
			topicrefSet.add("schemeref"); //$NON-NLS-1$
			topicrefSet.add("learningContentRef"); //$NON-NLS-1$
			topicrefSet.add("learningGroup"); //$NON-NLS-1$
			topicrefSet.add("learningGroupMapRef"); //$NON-NLS-1$
			topicrefSet.add("learningObject"); //$NON-NLS-1$
			topicrefSet.add("learningObjectMapRef"); //$NON-NLS-1$
			topicrefSet.add("learningOverviewRef"); //$NON-NLS-1$
			topicrefSet.add("learningPlanRef"); //$NON-NLS-1$
			topicrefSet.add("learningPostAssessmentRef"); //$NON-NLS-1$
			topicrefSet.add("learningPreAssessmentRef"); //$NON-NLS-1$
			topicrefSet.add("learningSummaryRef"); //$NON-NLS-1$
		}
		return topicrefSet.contains(name);
	}

	protected static Element getMatched(String name, Element topicmeta) {
		if (topicmeta.getName().equals(name)) {
			return topicmeta;
		} 
		List<Element> children = topicmeta.getChildren();
		Iterator<Element> it = children.iterator();
		while (it.hasNext()) {
			Element child = it.next();
			if (child.getName().equals(name)) {
				return child;
			}
			Element res = getMatched(name, child);
			if (res != null) {
				return res;
			}
		}
		return null;
	}

	protected static boolean ditaClass(Element e, String string) {
		String cls = e.getAttributeValue("class", ""); //$NON-NLS-1$ //$NON-NLS-2$
		String[] parts = cls.split("\\s"); //$NON-NLS-1$
		for (int i=0 ; i<parts.length ; i++) {
			if (parts[i].equals(string)) {
				return true;
			}
		}
		return false;
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

	private Element getReferenced(String file, String id, ILogger logger) throws SAXException, IOException, CancelException, ParserConfigurationException {
		StringArray array = new StringArray(file, id);
		Document doc = builder.build(file);
		Element root = doc.getRootElement();
		String topicId = root.getAttributeValue("id","");   //$NON-NLS-1$ //$NON-NLS-2$
		if (topicId.equals(id)) {
			if (!filesMap.contains(file)) {
				filesMap.add(file);
			}
			if (!searchedConref.contains(array)) {
				searchedConref.add(array);
				recurse(root, file, logger);
				searchedConref.remove(array);
			}
			return root;
		}
		Element result = locate(root, topicId, id);
		if (result != null) {
			if (!filesMap.contains(file)) {
				filesMap.add(file);
			}
			if (!searchedConref.contains(array)) {
				searchedConref.add(array);
				recurse(result, file, logger);
				searchedConref.remove(array);
			}
		}
		return result;
	}
	
	private Element locate(Element e, String topicId, String searched) {
		String id = e.getAttributeValue("id","");   //$NON-NLS-1$ //$NON-NLS-2$
		if (searched.equals(topicId + "/" + id)) {  //$NON-NLS-1$
			return e;
		}
		if (ditaClass(e, "topic/topic") || isTopic(e.getName())) { //$NON-NLS-1$
			topicId = id;
		}
		List<Element> children = e.getChildren();
		Iterator<Element> it = children.iterator();
		while (it.hasNext()) {
			Element child = it.next();
			Element result = locate(child, topicId, searched);
			if (result != null) {
				return result;
			}
		}
		return null;
	}
	
	private static boolean isTopic(String name) {
		if (topicSet == null) {
			topicSet = new TreeSet<String>();
			topicSet.add("topic"); //$NON-NLS-1$
			topicSet.add("concept"); //$NON-NLS-1$
			topicSet.add("glossentry"); //$NON-NLS-1$
			topicSet.add("reference"); //$NON-NLS-1$
			topicSet.add("task"); //$NON-NLS-1$
			topicSet.add("troubleshooting"); //$NON-NLS-1$
			topicSet.add("glossgroup"); //$NON-NLS-1$
			topicSet.add("learningAssessment"); //$NON-NLS-1$
			topicSet.add("learningBase"); //$NON-NLS-1$
			topicSet.add("learningContent"); //$NON-NLS-1$
			topicSet.add("learningOverview"); //$NON-NLS-1$
			topicSet.add("learningSummary"); //$NON-NLS-1$
			topicSet.add("learningPlan"); //$NON-NLS-1$
		}
		return topicSet.contains(name);
	}

	protected Element getConKeyReferenced(String file, String id, ILogger logger) throws SAXException, IOException, ParserConfigurationException {
		Document doc = builder.build(file);
		Element root = doc.getRootElement();
		return locateReferenced(root, id);
	}
	
	private Element locateReferenced(Element root, String id) {
		String current = root.getAttributeValue("id",""); //$NON-NLS-1$ //$NON-NLS-2$
		if (current.equals(id)) {
			return root;
		}
		List<Element> children = root.getChildren();
		Iterator<Element> it = children.iterator();
		while (it.hasNext()) {
			Element child = it.next();
			Element result = locateReferenced(child, id);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	public Scope getScope() {
		return rootScope;
	}

	public Hashtable<Key, Set<String>> getUsedKeys() {
		return usedKeys;
	}
	
	public Hashtable<String, Set<String>> getUsedAs() {
		return usedAs;
	}
}
