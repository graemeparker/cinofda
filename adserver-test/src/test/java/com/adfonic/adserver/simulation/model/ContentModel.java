package com.adfonic.adserver.simulation.model;

public class ContentModel {

	private String contentType;
	private byte[] raw;
	private String rawFile;
	private String json;
	private String jsonFile;
	private String proto;
	private String protoFile;

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public byte[] getRaw() {
		return raw;
	}

	public void setRaw(byte[] raw) {
		this.raw = raw;
	}

	public String getJson() {
		return json;
	}

	public void setJson(String json) {
		this.json = json;
	}

	public String getRawFile() {
		return rawFile;
	}

	public void setRawFile(String rawFile) {
		this.rawFile = rawFile;
	}

	public String getJsonFile() {
		return jsonFile;
	}

	public void setJsonFile(String jsonFile) {
		this.jsonFile = jsonFile;
	}

	public String getProto() {
		return proto;
	}

	public void setProto(String proto) {
		this.proto = proto;
	}

	public String getProtoFile() {
		return protoFile;
	}

	public void setProtoFile(String protoFile) {
		this.protoFile = protoFile;
	}
}
