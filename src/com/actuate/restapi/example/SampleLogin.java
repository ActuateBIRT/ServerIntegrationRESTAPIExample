package com.actuate.restapi.example;

import java.io.IOException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.util.Asserts;

import com.actuate.restapi.commons.Const;
import com.actuate.restapi.commons.Request;
import com.actuate.restapi.commons.Response;
import com.actuate.restapi.commons.Util;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Shows how to perform a user login. Login response contains Authentication Id
 * (AuthId), which must be used in all other requests. AuthId can be send as one
 * of the request parameters, of in HTTP request header. 
 */
public class SampleLogin {
	
	public static void main(String[] args) throws ClientProtocolException, IOException {
		loginAsAdministrator();
		loginAsNonExistingUser();
	}
	
	/**
	 * Login as admin user
	 */
	public static void loginAsAdministrator() throws ClientProtocolException, IOException {
		Util.log("\n### " + Util.getMethodName());
		Request req = new Request();
		Response res = req.auth(Const.ADMIN_USER, "");
		int status = res.getStatusCode();
		Asserts.check(status == 200, "Login is failed");
		Util.log("Status code: " + status);
		JsonElement authId = res.getBody().get("AuthId");
		Asserts.check(authId != null && authId.getAsString().length() > 0, "Authentication Id cannot be blank");
		Util.log("AuthId = " + authId);
	}
	
	/**
	 * An example of an error response. Shows error response structure,
	 * which is the same for all errors returned by REST server. The HTTP
	 * response code can be different, of course.
	 */
	public static void loginAsNonExistingUser() throws ClientProtocolException, IOException {
		Util.log("\n### " + Util.getMethodName());
		Request req = new Request();
		req.addParam("username", "Some User");
		req.addParam("password", "pass");
		Response res = req.post(req.getBaseUri() + "/login");
		int status = res.getStatusCode();
		Asserts.check(status == 401, "Login is supposed to fail");
		Util.log("Status code: " + status);
		JsonObject error = res.getBody().getAsJsonObject("error");
		Util.log("Error message: " + error.getAsJsonPrimitive("message").getAsString());
		Util.log("Error description: " + error.getAsJsonPrimitive("description").getAsString());
	}

}
