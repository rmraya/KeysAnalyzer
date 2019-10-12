package com.maxprograms.keysanalyzer.models;

import java.io.Serializable;
import java.util.Date;

public class Analysis implements Serializable {

	private static final long serialVersionUID = -3691558535508339335L;
	private String mapFile;
	private Date date;
	private String ditaVal;

	public Analysis(String mapFile, String ditaVal, Date date) {
		this.mapFile = mapFile;
		this.ditaVal = ditaVal;
		this.date = date;
	}

	public String getMapFile() {
		return mapFile;
	}

	public Date getDate() {
		return date;
	}

	public void setLastDate(Date value) {
		date = value;
	}

	public String getDitaval() {
		return ditaVal;
	}

}
