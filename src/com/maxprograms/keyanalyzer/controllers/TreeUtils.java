package com.maxprograms.keyanalyzer.controllers;

import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TreeSet;

public class TreeUtils {
	
	public static String findTreeRoot(TreeSet<String> set) {
		String result = ""; //$NON-NLS-1$
		Tree<String> tree = filesTree(set);
		Tree.Node<String> root = tree.getRoot();
		while (root.size() == 1) {
			result = result + root.getData();
			root = root.getChild(0);
		}		
		return result;
	}

	private static Tree<String> filesTree(TreeSet<String> files) {
		Tree<String> result = new Tree<String>(""); //$NON-NLS-1$
		Iterator<String> it = files.iterator();
		while (it.hasNext()) {
			String s = it.next();
			StringTokenizer st = new StringTokenizer(s, "/\\:", true); //$NON-NLS-1$
			Tree.Node<String> current = result.getRoot();
			while (st.hasMoreTokens()) {
				String name = st.nextToken();
				Tree.Node<String> level1 = current.getChild(name);
				if (level1 != null) {
					current = level1;
				} else {
					current.addChild(new Tree.Node<String>(name));
					current = current.getChild(name);
				}
			}
		}
		return result;
	}
}
