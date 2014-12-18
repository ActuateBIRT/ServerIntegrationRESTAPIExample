package com.actuate.restapi.example;

import java.io.File;
import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.Asserts;

import com.actuate.restapi.commons.Const;
import com.actuate.restapi.commons.Request;
import com.actuate.restapi.commons.Response;
import com.actuate.restapi.commons.Util;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * File and folder related examples. 
 */
public class SampleFile {
	
	private static final String sourceFileName = "SampleBIRTReport.rptdesign";
	private static String fileId;
	
	public static void main(String[] args) throws ClientProtocolException, IOException {
		new File(Const.DOWNLOAD_FOLDER).mkdirs();
		uploadFileUsingStreaming();
		uploadFileNoStreaming();
		downloadFileAsBinary();
		downloadFileAsBase64Encoded();
		grantPermissionsOnFile();
		getFilePermissions();
		getFolderItemsUsingFetch();
	}
	
	/**
	 * Upload file as a chunked stream. The client will not attempt to calculate the
	 * HTTP body size, which may give performance advantage when uploading larger files.
	 * Other methods in the current class use fileId defined here, so this method must
	 * run before any other one in the class.
	 */
	public static void uploadFileUsingStreaming() throws ClientProtocolException, IOException {
		Util.log("\n### " + Util.getMethodName());
		Request req = new Request();
		req.auth(Const.ADMIN_USER, "");
		String targetFolder = "/" + Const.RUN_ID + Util.getMethodName();
		req.addParam("name", targetFolder + "/" + sourceFileName);
		req.addParam("replaceExisting", "true");
		// currently fileName URI parameter is not used by REST server,
		// so it can be different from the actual file name (see below)
		Response res = req.upload(req.getBaseUri() + "/files/" + "fileToUpload" + "/upload", new File(Const.RESOURCE_FOLDER + "/" + sourceFileName));
		Asserts.check(res.getStatusCode() == 201, "Failed to upload file " + sourceFileName);
		fileId = res.getBody().getAsJsonPrimitive("fileId").getAsString();
		Util.log("Uploaded " + sourceFileName + " as " + targetFolder + "/" + sourceFileName + " with Id=" + fileId);
	}
	
	/**
	 * Upload file using non-chunked HTTP request. This is typically less preferable than
	 * streaming with chunks, but may still be required in certain cases.
	 */
	public static void uploadFileNoStreaming() throws ClientProtocolException, IOException {
		Util.log("\n### " + Util.getMethodName());
		Request req = new Request();
		req.auth(Const.ADMIN_USER, "");
		String targetFolder = "/" + Const.RUN_ID + Util.getMethodName();
		req.addParam("name", targetFolder + "/" + sourceFileName);
		Response res = req.upload(req.getBaseUri() + "/files/someName/upload", new File(Const.RESOURCE_FOLDER + "/" + sourceFileName), false);
		Asserts.check(res.getStatusCode() == 201, "Failed to upload file " + sourceFileName);
		Util.log("Uploaded " + sourceFileName + " as " + targetFolder + "/" + sourceFileName);
	}
	
	/**
	 * Download file as-is, i.e. as binary in HTTP response body. This method typically
	 * requires less processing on client side, but involves Base64-decoding on server
	 * side, which put additional load on server.
	 */
	public static void downloadFileAsBinary() throws ClientProtocolException, IOException {
		Util.log("\n### " + Util.getMethodName());
		Request req = new Request();
		req.auth(Const.ADMIN_USER, "");
		Response res = req.download(fileId, false);
		Asserts.check(res.getStatusCode() == 200, "Failed to download file " + fileId);
		Util.log("Downloaded file with Id=" + fileId + " and saved as " + res.getAttachment().getAbsolutePath());
	}
	
	/**
	 * Download file as Base64-encoded HTTP response body. It also saves the
	 * downloaded file with a custom name, not the one in HTTP response header. 
	 */
	public static void downloadFileAsBase64Encoded() throws ClientProtocolException, IOException {
		Util.log("\n### " + Util.getMethodName());
		Request req = new Request();
		req.auth(Const.ADMIN_USER, "");
		Response res = req.download(fileId, true, Util.getMethodName() + ".rptdesign");
		Asserts.check(res.getStatusCode() == 200, "Failed to download file " + fileId);
		Util.log("Downloaded file with Id=" + fileId + " and saved as " + res.getAttachment().getAbsolutePath());
	}
	
	/**
	 * Shows how to grant permissions on a file. Such a request is different from
	 * others since it has a JSON body.  
	 */
	public static void grantPermissionsOnFile() throws ClientProtocolException, IOException {
		Util.log("\n### " + Util.getMethodName());
		Request req = new Request();
		req.auth(Const.ADMIN_USER, "");
		String name = "User" + Const.RUN_ID + Util.getMethodName();
		req.addParam("name", name);
		req.addParam("password", name);
		req.addParam("homeFolder", "/Home/" + name);
		Response res = req.post(req.getBaseUri() + "/users");
		Asserts.check(res.getStatusCode() == 201, "Failed to create user " + name);
		Util.log("Created user " + name);
		String permissions =
			"{\"UserName\": \"" + name + "\", \"AccessRight\": \"VRWE\"}," +
			"{\"UserGroupName\": \"All\", \"AccessRight\": \"VR\"}";
		req.setContentType(ContentType.APPLICATION_JSON);
		HttpEntity body = new StringEntity("{\"Permission\": [" + permissions + "]}");
		res = req.post(req.getBaseUri() + "/files/" + fileId + "/privileges", body);
		Asserts.check(res.getStatusCode() == 200, "Failed to apply permissions on file with Id=" + fileId);
		Util.log("Granted permissions on file with Id=" + fileId);
	}
	
	/**
	 * Retrieve permissions (and other properties) of a file. It depends on the
	 * previous method where permissions are set.
	 */
	public static void getFilePermissions() throws ClientProtocolException, IOException {
		Util.log("\n### " + Util.getMethodName());
		Request req = new Request();
		req.auth(Const.ADMIN_USER, "");
		Response res = req.get(req.getBaseUri() + "/files/" + fileId + "/privileges");
		Asserts.check(res.getStatusCode() == 200, "Failed to get permissions for file Id=" + fileId);
		JsonArray permissions = res.getBody().getAsJsonObject("ACL").getAsJsonArray("Permission");
		Asserts.check(permissions.size() == 2, "The number of permissions is not equal to 2, but " + permissions.size());
		Asserts.check(permissions.get(0).getAsJsonObject().get("UserName").getAsString().startsWith("User" + Const.RUN_ID), "Incorrect user name");
		Asserts.check("VRWE".equals(permissions.get(0).getAsJsonObject().get("AccessRight").getAsString()), "VRWE permission is expected");
		Asserts.check("All".equals(permissions.get(1).getAsJsonObject().get("RoleName").getAsString()), "User group All is expected");
		Asserts.check("VR".equals(permissions.get(1).getAsJsonObject().get("AccessRight").getAsString()), "VR permission is expected");
		Util.log("Permissions of file with Id=" + fileId + " are: " + permissions.toString());
	}
	
	/**
	 * Shows how to retrieve long list of files in a folder. REST API provides
	 * fetching mechanism, that allows to get the long list in smaller portions.
	 * One thing to remember here is that if a FetchHandle is present in response
	 * then there is more data to fetch.
	 * The example below fetches files from start to end and then in the opposite
	 * direction, beginning from the end of the list (sorted alphabetically).
	 */
	public static void getFolderItemsUsingFetch() throws ClientProtocolException, IOException {
		Util.log("\n### " + Util.getMethodName());
		Request req = new Request();
		req.auth(Const.ADMIN_USER, "");
		// create folder
		String folderName = Const.RUN_ID + Util.getMethodName();
		req.addParam("name", folderName);
		Response res = req.post(req.getBaseUri() + "/folders");
		Asserts.check(res.getStatusCode() == 201, "Failed to create folder " + folderName);
		// get folder id
		req.addParam("search", folderName);
		res = req.get(req.getBaseUri() + "/folders");
		JsonArray folders = res.getBody().getAsJsonObject("ItemList").getAsJsonArray("File");
		Asserts.check(folders.size() == 1, "Only one file is expected");
		String folderId = folders.get(0).getAsJsonObject().getAsJsonPrimitive("Id").getAsString();
		Asserts.check(res.getStatusCode() == 200, "Failed to get Id for folder " + folderName);
		Util.log("Got folder Id for " + folderName + ": " + folderId);
		// upload files
		int numberOfFiles = 35;
		for(int i = 0; i < numberOfFiles; i++) {
			req.addParam("name", "/" + folderName + "/" + i + sourceFileName);
			req.addParam("replaceExisting", "false");
			res = req.upload(req.getBaseUri() + "/files/fileName/upload", new File(Const.RESOURCE_FOLDER + "/" + sourceFileName));
			Asserts.check(res.getStatusCode() == 201, "Failed to upload file " + i + sourceFileName);
		}
		Util.log("Uploaded " + numberOfFiles + " files");
		// select files from start to end
		String fetchHandle = null;
		numberOfFiles = 0;
		Util.log("Fetching start to end");
		do {
			req.addParam("search", "*" + sourceFileName);
			req.addParam("fetchSize", "9");
			req.addParam("fetchHandle", fetchHandle);
			res = req.get(req.getBaseUri() + "/folders/" + folderId + "/items");
			Asserts.check(res.getStatusCode() == 200, "Failed to select folder items using search " + "*" + sourceFileName);
			JsonArray files = res.getBody().getAsJsonObject("ItemList").getAsJsonArray("File");
			Util.log("Fetched another portion of files ...");
			for(int i = 0; i < files.size(); i++) {
				JsonObject file = files.get(i).getAsJsonObject();
				Util.log("Found file " + file.get("Name") + " with Id=" + file.get("Id"));
			}
			JsonElement fH = res.getBody().get("FetchHandle");
			fetchHandle = fH==null?null:fH.getAsString();
			numberOfFiles += files.size();
		} while(fetchHandle != null);
		Util.log("Totaly found " + numberOfFiles + " files while fetching start to end");
		// select files end to start
		fetchHandle = null;
		numberOfFiles = 0;
		Util.log("Fetching end to start");
		do {
			req.addParam("search", "*" + sourceFileName);
			//req.addParam("fetchSize", "9");
			req.addParam("fetchDirection", "false");
			req.addParam("fetchHandle", fetchHandle);
			res = req.get(req.getBaseUri() + "/folders/" + folderId + "/items");
			Asserts.check(res.getStatusCode() == 200, "Failed to select folder items using search " + "*" + sourceFileName);
			JsonArray files = res.getBody().getAsJsonObject("ItemList").getAsJsonArray("File");
			Util.log("Fetched another portion of files ...");
			for(int i = 0; i < files.size(); i++) {
				JsonObject file = files.get(i).getAsJsonObject();
				Util.log("Found file " + file.get("Name") + " with Id=" + file.get("Id"));
			}
			JsonElement fH = res.getBody().get("FetchHandle");
			fetchHandle = fH==null?null:fH.getAsString();
			numberOfFiles += files.size();
		} while(fetchHandle != null);
			Util.log("Totaly found " + numberOfFiles + " files while fetching end to start");
	}
	
}
