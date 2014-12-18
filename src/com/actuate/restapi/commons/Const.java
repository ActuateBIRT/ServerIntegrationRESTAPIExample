package com.actuate.restapi.commons;

public class Const {
	public static final Long RUN_TIMESTAMP = System.currentTimeMillis();
	public static final String RUN_ID = Long.toHexString(RUN_TIMESTAMP);
	
	public static final String RESOURCE_FOLDER = "./resources";
	public static final String DOWNLOAD_FOLDER = "./download";
	public static final String TEMP_FILE_PREFIX = "RESTAPISample" + Const.RUN_ID;
	
	public static final String ADMIN_USER = "Administrator";
	
	public static final String DEFAULT_REST_SERVER_HOST = "localhost";
	public static final String DEFAULT_REST_SERVER_PORT = "5000";
	public static final String DEFAULT_VOLUME = "Default Volume";
}
