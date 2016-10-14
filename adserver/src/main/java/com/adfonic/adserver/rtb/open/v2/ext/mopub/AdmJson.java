package com.adfonic.adserver.rtb.open.v2.ext.mopub;

import java.util.List;

public class AdmJson {
	private String iconimage;
	private String mainimage; 
	private String title;
	private String text;
	private String ctatext;
	private String clk;
	private List<String> imptracker;
	
	public String getIconimage() {
		return iconimage;
	}
	public void setIconimage(String iconimage) {
		this.iconimage = iconimage;
	}
	public String getMainimage() {
		return mainimage;
	}
	public void setMainimage(String mainimage) {
		this.mainimage = mainimage;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getCtatext() {
		return ctatext;
	}
	public void setCtatext(String ctatext) {
		this.ctatext = ctatext;
	}
	public String getClk() {
		return clk;
	}
	public void setClk(String clk) {
		this.clk = clk;
	}
	public List<String> getImptracker() {
		return imptracker;
	}
	public void setImptracker(List<String> imptracker) {
		this.imptracker = imptracker;
	}
}
