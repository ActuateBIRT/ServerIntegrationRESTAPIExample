package com.actuate.restapi.commons;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.Asserts;
import org.apache.http.util.EntityUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Request {
	
	private String host;
	private String port;
	
	private List<NameValuePair> params;
	private Map<String, String> headers;
	
	private boolean useSsl;
	
	private final String baseUri = "/ihub/v1";

	// constructors
	public Request() {
		this.setHeaders(new HashMap<String, String>());
		this.setParams(new ArrayList<NameValuePair> ());
		Properties props = System.getProperties();
		this.setHost(props.getProperty("restapi.host")!=null?props.getProperty("restapi.host"):Const.DEFAULT_REST_SERVER_HOST);
		this.setPort(props.getProperty("restapi.port")!=null?props.getProperty("restapi.port"):Const.DEFAULT_REST_SERVER_PORT);
		this.addHeader("TargetVolume", props.getProperty("restapi.volume")!=null?props.getProperty("restapi.volume"):Const.DEFAULT_VOLUME);
		// set accept gzip to false to get uncompressed response from server
		// this is helpful e.g. for reading response in a tunnel
		this.setAcceptGzip(Boolean.getBoolean(props.getProperty("restapi.gzip")));
		// the default content-type is URL-encoded
//		this.setContentType(ContentType.APPLICATION_FORM_URLENCODED);
	}
	
	// getters/setters
	public List<NameValuePair> getParams() {
		return params;
	}
	public void setParams(List<NameValuePair> params) {
		this.params = params;
	}
	public void addParam(String name, String value) {
		this.getParams().add(new BasicNameValuePair(name, value));
	}
	public void addParams(List<NameValuePair> list) {
		this.getParams().addAll(list);
	}
	public void resetParams() {
		this.setParams(new ArrayList<NameValuePair> ());
	}
	public String getHost() {
		return this.host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getPort() {
		return this.port;
	}
	public void setPort(String port) {
		this.port = port;
	}
	public boolean getUseSsl() {
		return this.useSsl;
	}
	public void setUseSsl(boolean useSsl) {
		this.useSsl = useSsl;
	}
	private void setHeaders(HashMap<String, String> headers) {
		this.headers = headers;
	}
	public Map<String, String> getHeaders() {
		return this.headers;
	}
	public void addHeader(String name, String value) {
		this.getHeaders().put(name, value);
	}
	public void setContentType(ContentType contentType) {
   		this.getHeaders().put(HttpHeaders.CONTENT_TYPE, contentType.getMimeType() + "; " + contentType.getCharset().name());
	}
	public void setContentType(String mimeType) {
   		this.getHeaders().put(HttpHeaders.CONTENT_TYPE, mimeType + "; " + ContentType.create(mimeType, Charset.forName("UTF-8")));
	}
	public void setAcceptGzip(boolean acceptGzip) {
		if(acceptGzip) {
    		this.getHeaders().put(HttpHeaders.ACCEPT_ENCODING, "gzip,deflate");
    	} else {
    		this.getHeaders().put(HttpHeaders.ACCEPT_ENCODING, "gzip;q=0");
    	}
	}
	public String getHttpPrefix() {
		if(this.getUseSsl()) {
			return "https://";
		} else {
			return "http://";
		}
	}
	public String getConnectionString() {
		return this.getHttpPrefix() + this.getHost() + ":" + this.getPort();
	}
	public String getBaseUri() {
		return this.getConnectionString() + this.baseUri;
	}
	
	public ResponseHandler<Response> getResponseHandler() {
		// Create a custom response handler
        ResponseHandler<Response> responseHandler = new ResponseHandler<Response>() {

            public Response handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
            	Response res = new Response();
            	// get status
            	StatusLine status = response.getStatusLine();
            	res.setStatusCode(status.getStatusCode());
            	res.setReason(status.getReasonPhrase());
            	// get headers
            	Map<String, String> headers = new HashMap<String, String>();
            	for(Header h: response.getAllHeaders()) {
            		headers.put(h.getName(), h.getValue());
            	}
            	res.setHeaders(headers);
            	// get body
                HttpEntity entity = response.getEntity();
                Header contentType = entity.getContentType();
                if(contentType != null && contentType.toString().matches(".*application/json.*")) {
                	// process all responses except responses with attachment
                	String jsonContent = EntityUtils.toString(entity);
                	JsonParser parser = new JsonParser();
                	JsonObject obj = parser.parse(jsonContent).getAsJsonObject();
                	res.setBody(obj);
                	res.setAttachment(null);
                } else {
                	// response body as attachment
                	InputStream in = entity.getContent();
                	File out = File.createTempFile(Const.TEMP_FILE_PREFIX, null);
                	Util.saveToFile(in, out);
                	in.close();
                	res.setAttachment(out);
                	res.setBody(null);
                }
                return res;
            }
        };
        
        return responseHandler;
	}
	
	// authentication
	public Response auth(String userName, String password) throws ClientProtocolException, IOException {
		this.addParam("username", userName);
		this.addParam("password", password);
		Response res = this.post(this.getBaseUri() + "/login");
		int status = res.getStatusCode();
		Asserts.check(status >= 200 && status < 300, "Login failed: " + res.getReason());
		JsonElement authId = res.getBody().get("AuthId");
		if(authId != null) {
			//this.setAuthId(authId.getAsString());
			this.addHeader("AuthId", authId.getAsString());
		} else {
			Util.throwException("No AuthId has been found");
		}
		return res;
	}
	
	// HTTP methods
	public Response get(String uri) throws ClientProtocolException, IOException {
		Response res = null;
		CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
        	RequestBuilder req = RequestBuilder.get().setUri(uri);
        	Map<String, String> headers = this.getHeaders();
        	for(String key: headers.keySet()) {
        		req.addHeader(key, headers.get(key));
        	}
        	List<NameValuePair> params = this.getParams();
        	for(NameValuePair key: params) {
        		req.addParameter(key.getName(), key.getValue());
        	}
        	res = httpClient.execute(req.build(), this.getResponseHandler());
        } finally {
        	this.resetParams();
            httpClient.close();
        }
        return res;
	}
	
	public Response post(String uri, HttpEntity entity) throws ClientProtocolException, IOException {
		Response res = null;
		CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
        	RequestBuilder req = RequestBuilder.post().setUri(uri);
        	Map<String, String> headers = this.getHeaders();
        	for(String key: headers.keySet()) {
        		req.addHeader(key, headers.get(key));
        	}
        	if(entity == null) {
        		List<NameValuePair> params = this.getParams();
	        	for(NameValuePair key: params) {
	        		req.addParameter(key.getName(), key.getValue());
	        	}
        	} else {
        		req.setEntity(entity);
        	}
        	res = httpClient.execute(req.build(), this.getResponseHandler());
        } finally {
        	this.resetParams();
            httpClient.close();
        }
        return res;
	}
	
	public Response post(String uri) throws ClientProtocolException, IOException {
		return this.post(uri, null);
	}
	
	public Response put(String uri) throws ClientProtocolException, IOException {
		Response res = null;
		CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
        	RequestBuilder req = RequestBuilder.put().setUri(uri);
        	Map<String, String> headers = this.getHeaders();
        	for(String key: headers.keySet()) {
        		req.addHeader(key, headers.get(key));
        	}
        	List<NameValuePair> params = this.getParams();
        	for(NameValuePair key: params) {
        		req.addParameter(key.getName(), key.getValue());
        	}
        	res = httpClient.execute(req.build(), this.getResponseHandler());
        } finally {
        	this.resetParams();
            httpClient.close();
        }
        return res;
	}
	
	public Response delete(String uri) throws ClientProtocolException, IOException {
		Response res = null;
		CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
        	RequestBuilder req = RequestBuilder.delete().setUri(uri);
        	Map<String, String> headers = this.getHeaders();
        	for(String key: headers.keySet()) {
        		req.addHeader(key, headers.get(key));
        	}
        	List<NameValuePair> params = this.getParams();
        	for(NameValuePair key: params) {
        		req.addParameter(key.getName(), key.getValue());
        	}
        	res = httpClient.execute(req.build(), this.getResponseHandler());
        } finally {
        	this.resetParams();
            httpClient.close();
        }
        return res;
	}
	
	// Helper methods
	public Response upload(String uri, File file, boolean chunked) throws ClientProtocolException, IOException {
		Response res = null;
		CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
        	RequestBuilder req = RequestBuilder.post().setUri(uri);
        	Map<String, String> headers = this.getHeaders();
//        	this.setContentType(ContentType.MULTIPART_FORM_DATA);
        	for(String key: headers.keySet()) {
        		req.addHeader(key, headers.get(key));
        	}
        	MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        	builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        	builder.setCharset(Charset.forName("UTF-8"));
        	List<NameValuePair> params = this.getParams();
        	for(NameValuePair key: params) {
        		builder.addTextBody(key.getName(), key.getValue());
        	}
        	if(chunked) {
        		// HTTPClient detects that one of the parts is a stream and turns on chunks automatically
        		builder.addBinaryBody("file", new FileInputStream(file), ContentType.DEFAULT_BINARY, file.getName());
        	} else {
        		builder.addBinaryBody("file", file, ContentType.DEFAULT_BINARY, file.getName());
        	}
        	req.setEntity(builder.build());
        	res = httpClient.execute(req.build(), this.getResponseHandler());
        } finally {
            httpClient.close();
        }
        return res;
	}
	
	public Response upload(String uri, File file) throws ClientProtocolException, IOException {
		return upload(uri, file, true);
	}
	
	public Response download(String fileId, boolean base64, String outputFileName) throws ClientProtocolException, IOException {
		Response res = null;
		// the following line can be used as a workaround if Content-Encoding returned in response is invalid
		//CloseableHttpClient httpClient = HttpClients.custom().disableContentCompression().build();
		CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
        	RequestBuilder req = RequestBuilder.get().setUri(this.getBaseUri() + "/files/" + fileId + "/download");
        	Map<String, String> headers = this.getHeaders();
        	for(String key: headers.keySet()) {
        		req.addHeader(key, headers.get(key));
        	}
        	List<NameValuePair> params = this.getParams();
        	for(NameValuePair key: params) {
        		req.addParameter(key.getName(), key.getValue());
        	}
        	req.addParameter("base64Encode", Boolean.toString(base64));
        	res = httpClient.execute(req.build(), this.getResponseHandler());
        	Asserts.check(res.getAttachment() != null, "Received no attachment in response");
        	// move received file to download folder
        	String fileName = null;
        	if(outputFileName != null && outputFileName.length() > 0) {
        		fileName = outputFileName;
        	} else {
        		fileName = res.getHeaders().get("Content-disposition");
        		Asserts.check(fileName != null && fileName.length() > 0, "File name is not found");
        		if(fileName != null) {
        			fileName = fileName.replaceFirst(".*filename=\"", "").replaceFirst("\".*", "");
        		}
        	}
    		
        	File attachment = new File(Const.DOWNLOAD_FOLDER + "/" + Const.RUN_ID + "_" + fileId + "_" + fileName);
        	if(base64) {
        		Base64InputStream in = new Base64InputStream(new FileInputStream(res.getAttachment()));
        		Util.saveToFile(in, attachment);
        		in.close();
        		if(attachment.exists()) {
        			res.setAttachment(attachment);
        		}
        	} else {
	        	if(res.getAttachment().renameTo(attachment)) {
	        		res.setAttachment(attachment);
	        	}
        	}
        	
        } finally {
            httpClient.close();
        }
        
        return res;
	}
	
	public Response download(String fileId, boolean base64) throws ClientProtocolException, IOException {
		return this.download(fileId, base64, null);
	}

}