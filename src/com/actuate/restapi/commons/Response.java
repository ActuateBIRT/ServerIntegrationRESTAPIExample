package com.actuate.restapi.commons;

import java.io.File;
import java.util.Map;

import com.google.gson.JsonObject;

public class Response {
	
	private int statusCode;
	private String reason;
	private JsonObject body;
	private File attachment;
	private Map<String, String> headers;
	
	// constructor
	public Response() {
	}
	
	// getters/setters
	public int getStatusCode() {
		return this.statusCode;
	}
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}
	public String getReason() {
		return this.reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	public JsonObject getBody() {
		return this.body;
	}
	public void setBody(JsonObject body) {
		this.body = body;
	}
	public File getAttachment() {
		return this.attachment;
	}
	public void setAttachment(File attachment) {
		this.attachment = attachment;
	}
	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}
	public Map<String, String> getHeaders() {
		return this.headers;
	}
}
