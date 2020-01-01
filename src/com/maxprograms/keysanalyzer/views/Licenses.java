/*********************************************************************** 

Copyright (c) 2016-2020 - Maxprograms,  http://www.maxprograms.com/

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
package com.maxprograms.keysanalyzer.views;

import java.io.File;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.MalformedURLException;

import com.maxprograms.keysanalyzer.KeysAnalyzer;
import com.maxprograms.utils.Locator;
import com.maxprograms.widgets.CustomLink;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class Licenses {

    protected static final Logger LOGGER = System.getLogger(AboutBox.class.getName());

    protected Shell shell;
    private Display display;

    public Licenses(Shell parent, int style) {
        shell = new Shell(parent, style);
        shell.setText("Licenses");
        display = shell.getDisplay();
        shell.setLayout(new GridLayout(2, false));
        shell.setImage(KeysAnalyzer.getResourceManager().getIcon());
        shell.addListener(SWT.Close, new Listener() {

            @Override
            public void handleEvent(Event arg0) {
                Locator.remember(shell, "Licenses");
            }
        });

        Label keysAnalyzer = new Label(shell, SWT.NONE);
        keysAnalyzer.setText("KeysAnalyzer");

        CustomLink keysAnalyzerLink = new CustomLink(shell, SWT.NONE);
        keysAnalyzerLink.setText("MIT License");
        try {
            keysAnalyzerLink.setURL(new File("lib/licenses/keyanalizer.txt").toURI().toURL().toString());
        } catch (MalformedURLException e) {
            LOGGER.log(Level.ERROR, "Error getting license", e);
        }

        Label java = new Label(shell, SWT.NONE);
        java.setText("Java Runtime Environment");

        CustomLink javaLink = new CustomLink(shell, SWT.NONE);
        javaLink.setText("GPL2 With Classpath Exception");
        try {
            javaLink.setURL(new File("lib/licenses/Java.html").toURI().toURL().toString());
        } catch (MalformedURLException e) {
            LOGGER.log(Level.ERROR, "Error getting license", e);
        }

        Label openxliff = new Label(shell, SWT.NONE);
        openxliff.setText("OpenXLIFF");

        CustomLink openxliffLink = new CustomLink(shell, SWT.NONE);
        openxliffLink.setText("Eclipse Public License Version 1.0");
        try {
            openxliffLink.setURL(new File("lib/licenses/EclipsePublicLicense1.0.html").toURI().toURL().toString());
        } catch (MalformedURLException e) {
            LOGGER.log(Level.ERROR, "Error getting license", e);
        }

        Label swt = new Label(shell, SWT.NONE);
        swt.setText("SWT");

        CustomLink swtLink = new CustomLink(shell, SWT.NONE);
        swtLink.setText("Eclipse Public License Version 1.0");
        try {
            swtLink.setURL(new File("lib/licenses/EclipsePublicLicense1.0.html").toURI().toURL().toString());
        } catch (MalformedURLException e) {
            LOGGER.log(Level.ERROR, "Error getting license", e);
        }

        Label mapDB = new Label(shell, SWT.NONE);
        mapDB.setText("MapDB");

        CustomLink mapdbLink = new CustomLink(shell, SWT.NONE);
        mapdbLink.setText("Apache License 2.0");
        try {
            mapdbLink.setURL(new File("lib/licenses/Apache2.0.html").toURI().toURL().toString());
        } catch (MalformedURLException e) {
            LOGGER.log(Level.ERROR, "Error getting license", e);
        }

        shell.pack();
    }

    public void show() {
        Locator.setLocation(shell, "Licenses");
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }
}