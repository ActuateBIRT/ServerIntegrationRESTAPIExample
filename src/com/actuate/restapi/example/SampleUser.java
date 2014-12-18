package com.actuate.restapi.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.Asserts;

import com.actuate.restapi.commons.Const;
import com.actuate.restapi.commons.Request;
import com.actuate.restapi.commons.Response;
import com.actuate.restapi.commons.Util;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Example of how to construct and send a typical request of each type:
 * POST, GET, PUT, DELETE. All examples in this class are for users,
 * but the same types of requests can be made for object of other types. 
 */
public class SampleUser {
	
	private static String userId = null;
	
	public static void main(String[] args) throws ClientProtocolException, IOException {
		createMultipleUsers();
		selectMultipleUsers();
		renameAUser();
		addGroupsToUser();
		deleteAUser();
	}
	
	/**
	 * POST request example: create user.
	 */
	public static void createMultipleUsers() throws ClientProtocolException, IOException {
		Util.log("\n### " + Util.getMethodName());
		Request req = new Request();
		req.auth(Const.ADMIN_USER, "");
		String userName = Const.RUN_ID + "user";
		int numberOfUsers = 25;
		for(int i = 1; i <= numberOfUsers; i++) {
			req.addParam("name", userName + i);
			req.addParam("password", userName + i);
			req.addParam("email", userName + i + "@xyz.com");
			req.addParam("description", "Description for user \"" + userName + i + "\"");
			req.addParam("homeFolder", "/Home/" + userName + i);
			Response res = req.post(req.getBaseUri() + "/users");
			Asserts.check(res.getStatusCode() == 201, "Failed to create user " + userName + i);
		}
		Util.log("Created " + numberOfUsers + " users");
	}
	
	/**
	 * GET request example: select users. It also shows how to use
	 * fetching for retrieving long item lists.
	 */
	public static void selectMultipleUsers() throws ClientProtocolException, IOException {
		Util.log("\n### " + Util.getMethodName());
		Request req = new Request();
		req.auth(Const.ADMIN_USER, "");
		String userName = Const.RUN_ID + "user";
		int numberOfUsers = 0;
		String fetchHandle = null;
		do {
			req.addParam("search", userName + "*");
			req.addParam("fetchSize", "9");
			req.addParam("fetchHandle", fetchHandle);
			Response res = req.get(req.getBaseUri() + "/users");
			Asserts.check(res.getStatusCode() == 200, "Failed to select users " + userName + "*");
			JsonArray users = res.getBody().getAsJsonObject("Users").getAsJsonArray("User");
			for(int i = 0; i < users.size(); i++) {
				JsonObject user = users.get(i).getAsJsonObject();
				Util.log("Found user " + user.get("Name") + " with id=" + user.get("Id"));
			}
			if(userId == null) {
				userId = users.get(0).getAsJsonObject().get("Id").getAsString();
			}
			JsonElement fH = res.getBody().get("FetchHandle");
			fetchHandle = fH==null?null:fH.getAsString();
			numberOfUsers += users.size();
		} while(fetchHandle != null);
		Util.log("Totaly found  " + numberOfUsers + " users");
	}
	
	/**
	 * PUT request example: rename a user.
	 */
	public static void renameAUser() throws ClientProtocolException, IOException {
		Util.log("\n### " + Util.getMethodName());
		Request req = new Request();
		req.auth(Const.ADMIN_USER, "");
		String userName = Const.RUN_ID + "RenamedUser";
		req.addParam("name", userName);
		Response res = req.put(req.getBaseUri() + "/users/" + userId);
		Asserts.check(res.getStatusCode() == 200, "Failed to rename user with Id=" + userId);
		res = req.get(req.getBaseUri() + "/users/" + userId);
		Asserts.check(res.getStatusCode() == 200, "Failed to get user properties (Id=" + userId + ")");
		JsonArray users = res.getBody().getAsJsonObject("Users").getAsJsonArray("User");
		Asserts.check(users.size() == 1, "Expected one user properties");
		String newUserName = users.get(0).getAsJsonObject().get("Name").getAsString();
		Asserts.check(userName.equals(newUserName), "User name is different: expected " + userName + " but found " + newUserName);
		Util.log("Renamed user with Id=" + userId + " to " + newUserName);
	}
	
	/**
	 * PUT request example: add user groups to a user. This also shows
	 * how to use requests with multiple parameters with the same name.
	 */
	public static void addGroupsToUser() throws ClientProtocolException, IOException {
		Util.log("\n### " + Util.getMethodName());
		Request req = new Request();
		req.auth(Const.ADMIN_USER, "");
		// create user groups
		String groupNameBase = Const.RUN_ID + Util.getMethodName() + "group";
		int groupCount = 5;
		for(int i = 1; i <= groupCount; i++) {
			req.addParam("name", groupNameBase + i);
			req.post(req.getBaseUri() + "/usergroups");
		}
		// get group Ids
		req.addParam("search", groupNameBase + "*");
		Response res = req.get(req.getBaseUri() + "/usergroups");
		Asserts.check(res.getStatusCode() == 200, "Select user groups failed");
		JsonArray groups = res.getBody().getAsJsonObject("UserGroups").getAsJsonArray("UserGroup");
		Asserts.check(groups.size() == groupCount, "Expected " + groupCount + " user groups, but found " + groups.size());
		List<NameValuePair> groupIds = new ArrayList<NameValuePair>();
		for(int i = 0; i < groupCount; i++) {
			groupIds.add(new BasicNameValuePair("addGroup", groups.get(i).getAsJsonObject().get("Id").getAsString()));
		}
		req.addParams(groupIds);
		res = req.put(req.getBaseUri() + "/users/" + userId);
		Asserts.check(res.getStatusCode() == 200, "Failed to add group to user with Id=" + userId);
		res = req.get(req.getBaseUri() + "/users/" + userId + "/usergroups");
		Asserts.check(res.getStatusCode() == 200, "Failed to get user's groups (Id=" + userId + ")");
		groups = res.getBody().getAsJsonObject("UserGroups").getAsJsonArray("UserGroup");
		Asserts.check(groups.size() == groupIds.size(), "Expected " + groupIds.size() + " user groups, but found " + groups.size());
		Util.log("Retrieved " + groups.size() + " user groups for user with Id=" + userId);
	}
	
	/**
	 * DELETE request example: delete a user.
	 */
	public static void deleteAUser() throws ClientProtocolException, IOException {
		Util.log("\n### " + Util.getMethodName());
		Request req = new Request();
		req.auth(Const.ADMIN_USER, "");
		Response res = req.delete(req.getBaseUri() + "/users/" + userId);
		Asserts.check(res.getStatusCode() == 200, "Failed to delete user with Id=" + userId);
		res = req.get(req.getBaseUri() + "/users/" + userId);
		Asserts.check(res.getStatusCode() == 200, "Expected user to be deleted by now (Id=" + userId + ")");
		JsonElement user = res.getBody().getAsJsonObject("Users").get("User");
		Asserts.check(user == null, "Expected user to be deleted by now (Id=" + userId + ")");
		Util.log("Deleted user with Id=" + userId);
	}
	
}
