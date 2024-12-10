package com.mcinfotech.event.domain;

import java.io.Serializable;

public class ProjectInfo implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3961934081835924206L;
	private long id;
	private String name;
	private String source;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	@Override
	public String toString() {
		return "ProjectInfo [id=" + id + ", name=" + name + ", source=" + source + "]";
	}
}
