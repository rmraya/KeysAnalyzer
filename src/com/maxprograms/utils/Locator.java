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

import java.io.IOException;
import java.util.Hashtable;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

import com.maxprograms.keysanalyzer.Constants;


public class Locator {
	
	public static void setLocation(Shell shell, String type) {
		try {
			Hashtable<String, String> values = Preferences.getInstance(Constants.PREFERENCES).get(type);
			if (values.size() > 0) {
				Point location = new Point(Integer.parseInt(values.get("X")), Integer.parseInt(values.get("Y"))); //$NON-NLS-1$ //$NON-NLS-2$
				shell.setLocation(location);
			}
		} catch (IOException ioe){
			ioe.printStackTrace();
		}
	}

	public static void position(Shell shell, String type) {
		try {
			Hashtable<String, String> values = Preferences.getInstance(Constants.PREFERENCES).get(type);
			if (values.size() > 0) {
				Point location = new Point(Integer.parseInt(values.get("X")), Integer.parseInt(values.get("Y"))); //$NON-NLS-1$ //$NON-NLS-2$
				shell.setLocation(location);
				Point size = new Point(Integer.parseInt(values.get("Width")), Integer.parseInt(values.get("Height"))); //$NON-NLS-1$ //$NON-NLS-2$
				shell.setSize(size);
			}
		} catch (IOException ioe){
			ioe.printStackTrace();
		}
	}

	public static void remember(Shell shell, String type) {
		try {
			Hashtable<String, String> values = new Hashtable<String, String>();
			values.put("X", "" + shell.getLocation().x); //$NON-NLS-1$ //$NON-NLS-2$
			values.put("Y", "" + shell.getLocation().y); //$NON-NLS-1$ //$NON-NLS-2$
			values.put("Width", "" + shell.getSize().x); //$NON-NLS-1$ //$NON-NLS-2$
			values.put("Height", "" + shell.getSize().y); //$NON-NLS-1$ //$NON-NLS-2$
			Preferences.getInstance(Constants.PREFERENCES).save(type, values);
		} catch (IOException ioe){
			ioe.printStackTrace();
		}
	}

}
