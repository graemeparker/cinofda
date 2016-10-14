package com.adfonic.adserver.simulation.model;

public class SimulationModel {

	private String description;
	private AdserverDataModel adserverData;
	private RequestDataModel request;
	private ResponseDataModel response;
	private AdserverOutputModel output;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public AdserverDataModel getAdserverData() {
		return adserverData;
	}

	public void setAdserverData(AdserverDataModel adserverData) {
		this.adserverData = adserverData;
	}

	public RequestDataModel getRequest() {
		return request;
	}

	public void setRequest(RequestDataModel request) {
		this.request = request;
	}

	public ResponseDataModel getResponse() {
		return response;
	}

	public void setResponse(ResponseDataModel response) {
		this.response = response;
	}

	public AdserverOutputModel getOutput() {
		return output;
	}

	public void setOutput(AdserverOutputModel output) {
		this.output = output;
	}

}
