package com.hps.mayo.configuration;

public final class AppConstants {

	public final static String EMPTY_STRING = "";
	public final static String SPACE = " ";
	public final static String UNDETERMINED = "Undetermined";
	public final static String NEW_LINE= "\n";

	// Logging constants
	public final static String ERROR_CRITICAL = "CRITICAL";
	public final static String ERROR_WARN = "WARNING";
	public final static String ERROR_KEY = "ERROR_MESSAGE";

	// REST constants
	public final static String AUTHORIZATION_URI = "/e360/com.informatica.tools.mdm.web.auth/login";
	public final static String PUT_URI = "mdm/interface/services/put";
	public final static String MERGE_URI = "mdm/interface/services/merge";
	public final static String UMERGE_URI = "mdm/interface/services/unmerge";
	public final static String PING_URI = "mdm/interface/services/ping";

	// Endpoint constants
	public final static String MDM_URI_PREFIX = "/cmx/cs/";
	public static final String PATH_DELIMITER = "/";

	public static final String EXECUTION_SUCCESS_IND = "SUCCESS";
	public static final String EXECUTION_FAILURE_IND = "FAILIURE";

	private AppConstants() {
		throw new AssertionError();
	}

}
