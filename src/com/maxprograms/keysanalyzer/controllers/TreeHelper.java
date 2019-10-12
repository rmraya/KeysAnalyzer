package com.maxprograms.keysanalyzer.controllers;

import java.io.File;
import java.util.Iterator;
import java.util.Stack;
import java.util.TreeSet;

import com.maxprograms.keysanalyzer.controllers.Tree.Node;

public class TreeHelper {

	public static Tree<String> filesTree(TreeSet<String> files) {
		Tree<String> result = new Tree<String>("");
		Iterator<String> it = files.iterator();
		while (it.hasNext()) {
			String s = it.next();
			File f = new File(s);
			Stack<String> stack = new Stack<String>();
			stack.add(f.getName());
			File parent = f.getParentFile();
			while (parent != null) {
				if (parent.getName().equals("")) {
					break;
				}
				stack.push(parent.getName());
				parent = parent.getParentFile();
			}
			Node<String> current = result.getRoot();
			while (stack.size() >0) {
				String name = stack.pop();
				Node<String> level1 = current.getChild(name);
				if (level1 != null) {
					current = level1;
				} else {
					current.addChild(new Node<String>(name));
					current = current.getChild(name);
				}
			}
		}
		return result;
	}
}