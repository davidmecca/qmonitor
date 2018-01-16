package com.hps.mayo.application.utilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class CommonUtils {

	public static String nl = "\n";
	private static final Logger tivoliLogger1 = LogManager.getLogger("TivoliLogger1");
	private static final Logger tivoliLogger2 = LogManager.getLogger("TivoliLogger2");

	protected static String serverEnv = null;
	protected static boolean loaded = false;
	protected static int thisNode = 0;
	protected static StringBuilder sb = new StringBuilder();
	
	public static final String appName = "MDM_PartyServices";
	public static final String defaultMsgId = "MDM_PUB";
	public static final String defaultAlertType = "ApplicationError";
	public static final String defaultCINumber = "CI00053397"; // CI Name = 'Informatica MDM'
	public static final String defaultStatus = "N/A";
	public static final String defaultSeverity = "ERROR";
	
	public CommonUtils() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * This method formats a message, adding metadata needed by the Tivoli log monitoring system, 
	 * then logs to both Syslog streams.
	 * 
	 * @param msg - The message you wish to format for logging.
	 * @param msgId - an unique message Id. Default is the prefix for this app, 'MDM_PUB'.
	 * @param alertType - e.g. 'QueueDepth', 'GCError', etc.  Default is 'ApplicationError'
	 * @param severity - CRIT / ERROR / WARN / INFO / etc.  Default is Error.
	 * @param ciNum - Service now CI number you want any incidents associated with.  
	 *                Default is "CI00053397" (CI Name = 'Informatica MDM').
	 *                NOTE:  if 'Round-a-bout' method is desired, use 'TBD' in this field.
	 * @param status - OPEN / CLOSED / etc -- not apply to everything, but can be useful. 
	 *                 Check w/ Tivoli team for details.
	 *                 Default is 'N/A'.
	 * @return
	 */
	public static void log2Tivoli(String msg, String msgId, String alertType, String severity, String ciNum, String status) {
		if (msg == null) {
			return;
		}
		
		// First format the message.
		sb.setLength(0);  // clear the buffer.
		sb.append("AppName=").append(appName).append("|");

		sb.append("Identifer=");
		if (msgId == null) {
			sb.append(defaultMsgId);
		}
		else {
			sb.append(msgId);
		}
		sb.append("|");
		
		sb.append("AlertType=");
		if (alertType == null) {
			sb.append(defaultAlertType);
		}
		else {
			sb.append(alertType);
		}
		sb.append("|");
		
		sb.append("CINum=");
		if (ciNum == null) {
			sb.append(defaultCINumber);
		}
		else {
			sb.append(ciNum);
		}
		sb.append("|");

		sb.append("GroupName=MDM - Master Data Management");
		sb.append("|");
		
		sb.append("Status=");
		if (status == null) {
			sb.append(defaultStatus);
		}
		else {
			sb.append(status);
		}
		sb.append("|");
		
		sb.append("Severity=");
		if (severity == null) {
			sb.append(defaultSeverity);
		}
		else {
			sb.append(severity);
		}
		sb.append("|Message=").append(msg).append(nl);
		
		// Log the message.
		String msgOut = sb.toString();
		tivoliLogger1.error(msgOut);
		tivoliLogger2.error(msgOut);
	}
	
}
