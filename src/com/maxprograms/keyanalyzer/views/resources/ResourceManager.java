package com.maxprograms.keyanalyzer.views.resources;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class ResourceManager {

	private Display display;
	private Image icon;
	private Image background;
	private Image macBackground;
	private Image help;
	private Image gear;
	private Image paper;
	private Image logo;

	public ResourceManager(Display display) {
		this.display = display;
	}

	public Image getIcon() {
		if (icon == null) {
			if (System.getProperty("os.name").startsWith("Mac")) {
				icon = new Image(display, ResourceManager.class.getResourceAsStream("macIcon.png")); //$NON-NLS-1$
			} else {
				icon = new Image(display, ResourceManager.class.getResourceAsStream("winIcon.png")); //$NON-NLS-1$
			}
		}
		return icon;
	}

	public Image getLogo() {
		if (logo == null) {
			logo = new Image(display, ResourceManager.class.getResourceAsStream("icon.png")); //$NON-NLS-1$
		}
		return logo;
	}
	public Image getMacBackground() {
		if (macBackground == null) {
			macBackground = new Image(display, ResourceManager.class.getResourceAsStream("macBackground.png")); //$NON-NLS-1$
		}
		return macBackground;
	}

	public Image getBackground() {
		if (background == null) {
			background = new Image(display, ResourceManager.class.getResourceAsStream("background.png")); //$NON-NLS-1$
		}
		return background;
	}
	
	public Image getPaper() {
		if (paper == null) {
			paper = new Image(display, ResourceManager.class.getResourceAsStream("paper.png")); //$NON-NLS-1$
		}
		return paper;
	}

	public Image getGear() {
		if (gear == null) {
			gear = new Image(display, ResourceManager.class.getResourceAsStream("gear.png")); //$NON-NLS-1$
		}
		return gear;
	}

	public Image getHelp() {
		if (help == null) {
			help = new Image(display, ResourceManager.class.getResourceAsStream("help.png")); //$NON-NLS-1$
		}
		return help;
	}
}
