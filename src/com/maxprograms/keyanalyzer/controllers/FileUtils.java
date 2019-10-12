package com.maxprograms.keyanalyzer.controllers;

import java.io.File;
import java.io.IOException;
import java.util.Vector;


public class FileUtils {

	public static String getAbsolutePath(String homeFile, String relative) throws IOException{
	   	File home = new File(homeFile);
	   	// If home is a file, get the parent
	   	File result;
	   	if (!home.isDirectory()){
	   		home = home.getParentFile();
	   	}
	   	result = new File(home, relative);	   		
	   	return result.getCanonicalPath();
	}

	public static String getRelativePath(String homeFile, String filename) throws IOException  {
		File home = new File(homeFile);
		// If home is a file, get the parent
		if (!home.isDirectory()) {
			if (home.getParent() != null) {
				home = new File(home.getParent());	
			} else {
				home = new File(System.getProperty("user.dir")); 
			}
			
		}
		File file = new File(filename);
		if (!file.isAbsolute()) {
			return filename;
		}
		// Check for relative path
		if (!home.isAbsolute()) {
			throw new IOException("Path must be absolute."); 
		}
		Vector<String> homelist;
		Vector<String> filelist;

		homelist = getPathList(home);
		filelist = getPathList(file);
		return matchPathLists(homelist, filelist);
	}
	
	private static Vector<String> getPathList(File file) throws IOException{
		Vector<String> list = new Vector<String>();
		File r;
		r = file.getCanonicalFile();
		while(r != null) {
			list.add(r.getName());
			r = r.getParentFile();
		}
		return list;
	}
	
	private static String matchPathLists(Vector<String> r, Vector<String> f) {
		int i;
		int j;
		String s = ""; 
		// start at the beginning of the lists
		// iterate while both lists are equal
		i = r.size()-1;
		j = f.size()-1;

		// first eliminate common root
		while(i >= 0&&j >= 0&&r.get(i).equals(f.get(j))) {
			i--;
			j--;
		}

		// for each remaining level in the home path, add a ..
		for(;i>=0;i--) {
			s += ".." + File.separator; 
		}

		// for each level in the file path, add the path
		for(;j>=1;j--) {
			s += f.get(j) + File.separator;
		}

		// file name
		if ( j>=0 && j<f.size()) {
			s += f.get(j);
		}
		return s;
	}
	
}
