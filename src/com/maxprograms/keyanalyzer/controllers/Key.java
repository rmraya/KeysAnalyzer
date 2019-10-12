package com.maxprograms.keyanalyzer.controllers;

import com.maxprograms.xml.Element;

public class Key implements Comparable<Key> {

	private String name;
	private String href;
	private Element topicmeta;
	private String keyref;
	private String defined;
	
	public Key(String name, String keyref, String defined) {
		this.name = name;
		this.keyref = keyref;
		this.defined = defined;
	} 
	
	public Key(String name, String href, Element topicmeta, String defined) {
		this.name = name;
		this.href = href;
		this.topicmeta = topicmeta;
		this.defined = defined;
	}

	public String getName() {
		return name;
	}

	public String getHref() {
		return href;
	}

	public String getKeyref() {
		return keyref;
	}
	
	public Element getTopicmeta() {
		return topicmeta;
	}
	
	public String getDefined() {
		return defined;
	}

	@Override
	public int compareTo(Key o) {
		if (name.equals(o.getName())) {
			return 0;
		};
		return 1;
	}
	
}
