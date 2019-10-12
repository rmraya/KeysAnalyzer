package com.maxprograms.keysanalyzer.controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.maxprograms.keysanalyzer.controllers.Tree.Node;
import com.maxprograms.keysanalyzer.models.Analysis;
import com.maxprograms.widgets.AsyncLogger;

public class Analyzer {

	private static FileOutputStream output;
	private static int level;

	public static void analize(Analysis analysis, AsyncLogger alogger) {
		DitaParser parser = new DitaParser();
		Hashtable<String,String> params = new Hashtable<String,String>();
		params.put("source", analysis.getMapFile()); 
		params.put("catalogue", "catalog/catalog.xml");
		String val = analysis.getDitaval();
		if (val != null) {
			params.put("ditaval", val);
		}
		try {
			Vector<String> files = parser.run(params, alogger);
			if (alogger != null) {
				if (alogger.isCancelled()) {
					throw new CancelException("User cancelled.");
				}
			}
			Scope scope = parser.getScope();
			Hashtable<Key, Set<String>> usedKeys = parser.getUsedKeys();
			Hashtable<String, Set<String>> usedAs = parser.getUsedAs();
			if (alogger != null) {
				if (alogger.isCancelled()) {
					throw new CancelException("User cancelled.");
				}
				alogger.setStage("Preparing Report");
				alogger.log("Writing HTML files...");
			}
			report(analysis.getMapFile(), files, scope, usedKeys, usedAs, alogger.getErrors());
			alogger.displaySuccess("Parsing completed.");
		} catch (IOException | SAXException | ParserConfigurationException | CancelException | URISyntaxException e) {
			e.printStackTrace();
			alogger.displayError(e.getMessage());
		}
	}

	private static void report(String mapFile, Vector<String> files, Scope scope, Hashtable<Key, Set<String>> usedKeys, Hashtable<String, Set<String>> usedAs, Vector<String> errors) throws UnsupportedEncodingException, IOException {
		File map = new File(mapFile);
		File out = new File(map.getParentFile(), "out");
		if (!out.exists()) {
			out.mkdirs();
		}
		File keysFolder = new File(out, "keys");
		if (!keysFolder.exists()) {
			keysFolder.mkdirs();
		}
		
		File parsedFiles = new File(keysFolder, "files.html");
		output = new FileOutputStream(parsedFiles);
		
		TreeSet<String> treeSet = new TreeSet<String>(new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				return o1.toLowerCase().compareTo(o2.toLowerCase());
			}
		});
		treeSet.addAll(files);
		String root = TreeUtils.findTreeRoot(treeSet);
		writeHeader(mapFile);

		writeStr("    <h1>" + cleanString(new File(mapFile).getName()) + "</h1>\n\n");
		
		writeStr("    <table width='100%' class='tabs'>\n");
		writeStr("      <tr>\n");
		writeStr("        <td class='selected'>Parsed&nbsp;Files</td>\n");
		if (errors.size()>0) {
			writeStr("        <td><a href='errors.html'>Errors</a></td>\n");
		}
		writeStr("        <td><a href='defined.html'>Defined&nbsp;Keys</a></td>\n");
		writeStr("        <td><a href='used.html'>Actually&nbsp;Used&nbsp;Keys</a></td>\n");
		writeStr("        <td width='100%' class='filler'>&nbsp;</td>\n");
		writeStr("      </tr>\n");
		writeStr("    </table>\n");
		
		
		writeStr("    <h2 id='files'>" + "Parsed Files" + "</h2>\n\n");
		
		writeStr("    <pre style='margin-left:40px'>\n");
		Tree<String> filesTree = TreeHelper.filesTree(treeSet);
		recurse(filesTree.prune().getRoot());
		writeStr("    </pre>\n\n");
		
		writeStr("  </body>\n");
		writeStr("</html>\n");
		
		output.close();
		
		if (errors.size()>0) {
			File used = new File(keysFolder, "errors.html");
			output = new FileOutputStream(used);
			writeHeader(mapFile);
			
			writeStr("    <h1>" + cleanString(new File(mapFile).getName()) + "</h1>\n\n");
			
			writeStr("    <table width='100%' class='tabs'>\n");
			writeStr("      <tr>\n");
			writeStr("        <td><a href='files.html'>Parsed&nbsp;Files</a></td>\n");
			writeStr("        <td class='selected'>Errors</td>\n");
			writeStr("        <td><a href='defined.html'>Defined&nbsp;Keys</a></td>\n");
			writeStr("        <td><a href='used.html'>Actually&nbsp;Used&nbsp;Keys</a></td>\n");
			writeStr("        <td width='100%' class='filler'>&nbsp;</td>\n");
			writeStr("      </tr>\n");
			writeStr("    </table>\n");
			
			writeStr("    <h2 id='errors'>" + "Errors" + "</h2>\n\n");
			writeStr("    <ul style='margin-left:40px'>\n");
			for (int i=0 ; i<errors.size() ; i++) {
				writeStr("      <li>" + cleanString(errors.get(i)) + "</li>\n");
			}
			writeStr("    </ul>\n\n");
			writeStr("  </body>\n");
			writeStr("</html>\n");
			
			output.close();
		}
			
		File defined = new File(keysFolder, "defined.html");
		output = new FileOutputStream(defined);
		writeHeader(mapFile);
		writeStr("    <h1>" + cleanString(new File(mapFile).getName()) + "</h1>\n\n");
		
		writeStr("    <table width='100%' class='tabs'>\n");
		writeStr("      <tr>\n");
		writeStr("<td><a href='files.html'>Parsed&nbsp;Files</a></td>\n");
		if (errors.size()>0) {
			writeStr("        <td><a href='errors.html'>Errors</a></td>\n");
		}
		writeStr("        <td class='selected'>Defined&nbsp;Keys</td>\n");
		writeStr("        <td><a href='used.html'>Actually&nbsp;Used&nbsp;Keys</a></td>\n");
		writeStr("        <td width='100%' class='filler'>&nbsp;</td>\n");
		writeStr("      </tr>\n");
		writeStr("    </table>\n");
			
		Hashtable<String, Key> keys = scope.getKeys();
		TreeSet<String> keySet = new TreeSet<String>(new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				return o1.toLowerCase().compareTo(o2.toLowerCase());
			}
		});
		keySet.addAll(keys.keySet());
		Iterator<String> kit = keySet.iterator();
		writeStr("    <h2 id='defined'>" + "Defined Keys" + "</h2>\n\n");
		
		if (keySet.size() != usedKeys.size()) {
			writeStr("    <p>Unused keys are marked in <span style='color:red;'>red</span>.</p>");
		}
		
		writeStr("    <table class='report'>\n");
		writeStr("      <tr>\n");
		writeStr("    	  <th>" + "Key" + "</th>\n");
		writeStr("    	  <th>" + "Defined" + "</th>\n");
		writeStr("    	  <th>" + "href" + "</th>\n");
		writeStr("      </tr>\n");
		while (kit.hasNext()) {
			String key = kit.next();
			Key k = keys.get(key);
			if (k.getHref() == null) {
				continue;
			}
			writeStr("      <tr>\n");
			if (usedKeys.containsKey(k)) {
				writeStr("    	  <td class='center'>" + cleanString(k.getName()) + "</td>\n");
			} else {
				writeStr("    	  <td class='center'><span style='color:red;'>" + cleanString(k.getName()) + "</span></td>\n");
			}
			writeStr("    	  <td class='left'>" + cleanString(k.getDefined().substring(root.length())) + "</td>\n");
			writeStr("    	  <td class='left'>" + cleanString(FileUtils.getRelativePath(k.getDefined(), k.getHref())) + "</td>\n");
			writeStr("      </tr>\n");
		}
		writeStr("    </table>\n\n");
		
		writeStr("  </body>\n");
		writeStr("</html>\n");
		
		File used = new File(keysFolder, "used.html");
		output = new FileOutputStream(used);
		writeHeader(mapFile);
		writeStr("    <h1>" + cleanString(new File(mapFile).getName()) + "</h1>\n\n");
		
		writeStr("    <table width='100%' class='tabs'>\n");
		writeStr("      <tr>\n");
		writeStr("        <td><a href='files.html'>Parsed&nbsp;Files</a></td>\n");
		if (errors.size()>0) {
			writeStr("        <td><a href='errors.html'>Errors</a></td>\n");
		}
		writeStr("        <td><a href='defined.html'>Defined&nbsp;Keys</a></td>\n");
		writeStr("        <td class='selected'>Actually&nbsp;Used&nbsp;Keys</td>\n");
		writeStr("        <td width='100%' class='filler'>&nbsp;</td>\n");
		writeStr("      </tr>\n");
		writeStr("    </table>\n");
		
		TreeSet<Key> usedKeySet = new TreeSet<Key>(new Comparator<Key>() {

			@Override
			public int compare(Key o1, Key o2) {
				return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
			}
		});
		usedKeySet.addAll(usedKeys.keySet());
		Iterator<Key> sid = usedKeySet.iterator();
		writeStr("    <h2 id='used'>" + "Actually Used Keys" + "</h2>\n\n");
		writeStr("    <table class='report'>\n");
		writeStr("      <tr>\n");
		writeStr("    	  <th>" + "Key" + "</th>\n");
		writeStr("    	  <th>" + "Used As" + "</th>\n");
		writeStr("    	  <th>" + "Used In" + "</th>\n");
		writeStr("      </tr>\n");
		int row = 0;
		String style = "";
		while (sid.hasNext()) {
			Key k = sid.next();
			Vector<String[]> usage = createList2(k, usedAs, root);
			String rowspan = "";
			if (usage.size() > 1) {
				rowspan = " rowspan='" + usage.size() + "'";
			}
			if (row++ % 2 == 1) {
				style = "_grey";
			} else {
				style = "";
			}
			writeStr("      <tr>\n");
			writeStr("    	  <td class='center'" + rowspan + ">" + cleanString(k.getName()) + "</td>\n");
			writeStr("    	  <td class='center" + style +"'>" + usage.get(0)[0] + "</td>\n"); 
			writeStr("    	  <td class='left" + style +"'>" + usage.get(0)[1] + "</td>\n"); 
			writeStr("      </tr>\n");
			for (int i=1 ; i<usage.size() ; i++) {
				if (row++ % 2 == 1) {
					style = "_grey";
				} else {
					style = "";
				}
				writeStr("      <tr>\n");
				writeStr("    	  <td class='center" + style +"'>" + usage.get(i)[0] + "</td>\n"); 
				writeStr("    	  <td class='left" + style +"'>" + usage.get(i)[1] + "</td>\n"); 
				writeStr("      </tr>\n");
			}
		}
		writeStr("    </table>\n");
		
		writeStr("  </body>\n");
		writeStr("</html>\n");
		
		output.close();
	}

	private static Vector<String[]> createList2(Key k, Hashtable<String, Set<String>> usedAs, String root) {
		Vector<String[]> result = new Vector<String[]>();
		TreeSet<String> set = new TreeSet<String>(new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				return o1.toLowerCase().compareTo(o2.toLowerCase());
			}
		});
		set.addAll(usedAs.keySet());
		Iterator<String> it = set.iterator();
		while (it.hasNext()) {
			String keyref = it.next();
			String key = keyref;
			if (keyref.indexOf("/") != -1) {
				key = keyref.substring(0, keyref.indexOf("/")); 
			}
			if (key.equals(k.getName())) {
				String string1 = cleanString(keyref);
				String string2 = "<ul>\n";
				// Set<String> list = usedAs.get(keyref);
				
				TreeSet<String> list = new TreeSet<String>(new Comparator<String>() {

					@Override
					public int compare(String o1, String o2) {
						return o1.toLowerCase().compareTo(o2.toLowerCase());
					}
				});
				list.addAll(usedAs.get(keyref));
				Iterator<String> f = list.iterator();
				while (f.hasNext()) {
					String file = f.next();
					string2 = string2 + "    <li>" + cleanString(file.substring(root.length())) + "</li>\n";
				}
				string2 = string2 + "   </ul>";
				result.add(new String[] {string1, string2});
			}
		}
		return result;
	}

	private static void writeStr(String string) throws UnsupportedEncodingException, IOException {
		output.write(string.getBytes("UTF-8"));
	}

	private static void recurse(Node<String> node) throws UnsupportedEncodingException, IOException {
		for (int i = 0 ; i<level ; i++) {
			writeStr("   ");
		}
		writeStr(cleanString(node.getData()) + "\n");
		level++;
		Iterator<Node<String>> it = node.iterator();
		while (it.hasNext()) {
			recurse(it.next());
		}		
		level--;
	}
	
	private static void writeHeader(String mapFile) throws UnsupportedEncodingException, IOException {
		writeStr("<html>\n");
		writeStr("  <head>\n");
		writeStr("    <meta charset=\"UTF-8\">\n");
		writeStr("    <title>" + cleanString(mapFile) + "</title>");
		writeStr("    <style type=\"text/css\">\n");
		writeStr("       table.report{\n");
		writeStr("           border-left:1px solid grey;\n");
		writeStr("           margin-left:40px;\n");
		writeStr("           font-family:sans-serif;\n");
		writeStr("       }\n");
		writeStr("       .report th{\n");
		writeStr("           border-left:1px solid grey;\n");
		writeStr("           border-right:1px solid grey;\n");
		writeStr("           background:#00796b;\n");
		writeStr("           color:white;\n");
		writeStr("           text-align:center;\n");
		writeStr("           padding:3px\n");
		writeStr("       }\n");
		writeStr("       .report td.left{\n");
		writeStr("           border-right:1px solid grey;\n");
		writeStr("           border-bottom:1px solid grey;\n");
		writeStr("           text-align:left;\n");
		writeStr("           padding:2px;\n");
		writeStr("       }\n");
		writeStr("       .report td.left_grey{\n");
		writeStr("           background:#eeeeee;\n");
		writeStr("           border-right:1px solid grey;\n");
		writeStr("           border-bottom:1px solid grey;\n");
		writeStr("           text-align:left;\n");
		writeStr("           padding:2px;\n");
		writeStr("       }\n");
		writeStr("       .report td.center{\n");
		writeStr("           border-right:1px solid grey;\n");
		writeStr("           border-bottom:1px solid grey;\n");
		writeStr("           text-align:center;\n");
		writeStr("           padding:2px;\n");
		writeStr("       }\n");
		writeStr("       .report td.center_grey{\n");
		writeStr("           background:#eeeeee;\n");
		writeStr("           border-right:1px solid grey;\n");
		writeStr("           border-bottom:1px solid grey;\n");
		writeStr("           text-align:center;\n");
		writeStr("           padding:2px;\n");
		writeStr("       }\n");
		writeStr("       .report td.right{\n");
		writeStr("           border-right:1px solid grey;\n");
		writeStr("           border-bottom:1px solid grey;\n");
		writeStr("           text-align:right;\n");
		writeStr("           padding:2px;\n");
		writeStr("       }\n");
		writeStr("       table.tabs{\n");
		writeStr("           border-bottom:2px solid #009688;\n");
		writeStr("           font-family:sans-serif;\n");
		writeStr("       }\n");
		writeStr("       .tabs td{\n");
		writeStr("           border-right:1px solid #009688;\n");
		writeStr("           border-left:1px solid #009688;\n");
		writeStr("           border-top:1px solid #009688;\n");
		writeStr("           border-top-left-radius:4px;\n");
		writeStr("           border-top-right-radius:4px;\n");
		writeStr("           text-align:center;\n");
		writeStr("           padding-left:10px;\n");
		writeStr("           padding-right:10px;\n");
		writeStr("           background:#009688;\n");
		writeStr("       }\n");
		writeStr("       .tabs td.selected{\n");
		writeStr("           border-right:1px solid #009688;\n");
		writeStr("           border-left:1px solid #009688;\n");
		writeStr("           border-top:1px solid #009688;\n");
		writeStr("           border-top-left-radius:4px;\n");
		writeStr("           border-top-right-radius:4px;\n");
		writeStr("           text-align:center;\n");
		writeStr("           padding-left:10px;\n");
		writeStr("           padding-right:10px;\n");
		writeStr("           background:white;\n");
		writeStr("           color:#009688;\n");
		writeStr("       }\n");
		writeStr("       .tabs td.filler{\n");
		writeStr("           border-right:none;\n");
		writeStr("           border-left:none;\n");
		writeStr("           border-top:none;\n");
		writeStr("           text-align:center;\n");
		writeStr("           padding-left:10px;\n");
		writeStr("           padding-right:10px;\n");
		writeStr("           background:white;\n");
		writeStr("       }\n");
		writeStr("       .tabs td a {\n");
		writeStr("           color:white;\n");
		writeStr("           text-decoration:none;\n");
		writeStr("       }\n");
		writeStr("       h1, h2 {\n");
		writeStr("           font-family:sans-serif;\n");
		writeStr("           color:#00695c;\n");
		writeStr("       }\n");
		writeStr("      </style>\n");
		writeStr("  </head>\n");
		writeStr("  <body>\n");
	}
	
	private static String cleanString(String string) {
		String result = string.replaceAll("&", "&amp;");
		return result.replaceAll("<", "&lt;");
	}
}
