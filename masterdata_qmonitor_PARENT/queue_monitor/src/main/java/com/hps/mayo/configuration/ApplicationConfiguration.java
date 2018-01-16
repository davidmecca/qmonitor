package com.hps.mayo.configuration;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
//import org.springframework.cloud.context.config.annotation.RefreshScope;

@Configuration
public class ApplicationConfiguration {

	private final Logger logger = LogManager.getLogger(this.getClass());

	@Value("${ws.endpoint.node1}")
	private String node1EndPoint;
	@Value("${ws.endpoint.node2}")
	private String node2EndPoint;
	@Value("${ws.context.root}")
	private String contextRoot;
	@Value("${hub.ors.id}")
	private String orsId;
	@Value("${hub.ping.endpoint}")
	private String hubPingEndpoint;
	@Value("${hub.user.id}")
	private String hubUserId;
	@Value("${hub.user.password}")
	private String hubUserPassword;
	@Value("${custom.mq.target.userid}")
	private String mqConnectionUser;
	@Value("${custom.mq.target.password}")
	private String mqConnectionUserPassword;
	@Value("${hl7.active.event.codes}")
	private String hl7ActiveEventCodes;
	@Value("${hl7.put.event.codes}")
	private String hl7PutEventCodes;
	@Value("${hl7.merge.event.codes}")
	private String hl7MergeEventCodes;
	@Value("${hl7.unmerge.event.codes}")
	private String hl7UnmergeEventCodes;
	@Value("${retry.interval.seconds:30}")
	private int retryIntervalSeconds;
	@Value("${failback.interval.seconds:30}")
	private int failbackIntervalSeconds;
	@Value("${max.retry.attempts:2}")
	private int maxRetryAttempts;
	@Value("${source.system.prefix:}")
	private String sourceSystemPrefix;
	@Value("${error.queue.max.successive.failures:10}")
	private int errorQueueMaxSuccessiveFailures;

	// Properties altered during runtime
	private static int numberOfNodes = 2;
	private static boolean isRunningOnAlternateNode = false;
	private static String activeEndPoint = null;
	private static int currentTargetServicesNode = 1; // Default to node 1 on
														// startup
	private static long lastFailoverStartTimeInMilliseconds = System.currentTimeMillis();
	private static int errorQueueSuccessiveFailureCounter = 0;

	public ApplicationConfiguration() {
	}

	@PostConstruct
	private void init() {

		activeEndPoint = node1EndPoint;

	}

	public String getNode1EndPoint() {
		return node1EndPoint;
	}

	public void setNode1EndPoint(String node1EndPoint) {
		this.node1EndPoint = node1EndPoint;
	}

	public String getNode2EndPoint() {
		return node2EndPoint;
	}

	public void setNode2EndPoint(String node2EndPoint) {
		this.node2EndPoint = node2EndPoint;
	}

	public String getContextRoot() {
		return contextRoot;
	}

	public void setContextRoot(String contextRoot) {
		this.contextRoot = contextRoot;
	}

	public static int getNumberOfNodes() {
		return numberOfNodes;
	}

	public static void setNumberOfNodes(int numberOfNodes) {
		ApplicationConfiguration.numberOfNodes = numberOfNodes;
	}

	public static boolean isRunningOnAlternateNode() {
		return isRunningOnAlternateNode;
	}

	public static void setRunningOnAlternateNode(boolean isRunningOnAlternateNode) {
		ApplicationConfiguration.isRunningOnAlternateNode = isRunningOnAlternateNode;
	}

	public static String getActiveEndPoint() {
		return activeEndPoint;
	}

	public static void setActiveEndPoint(String activeEndPoint) {
		ApplicationConfiguration.activeEndPoint = activeEndPoint;
	}

	public static int getCurrentTargetServicesNode() {
		return currentTargetServicesNode;
	}

	public static void setCurrentTargetServicesNode(int currentTargetServicesNode) {
		ApplicationConfiguration.currentTargetServicesNode = currentTargetServicesNode;
	}

	public static long getLastFailoverStartTimeInMilliseconds() {
		return lastFailoverStartTimeInMilliseconds;
	}

	public static void setLastFailoverStartTimeInMilliseconds(long lastFailoverStartTimeInMilliseconds) {
		ApplicationConfiguration.lastFailoverStartTimeInMilliseconds = lastFailoverStartTimeInMilliseconds;
	}

	public Logger getLogger() {
		return logger;
	}

	public String getHubUserId() {
		return hubUserId;
	}

	public void setHubUserId(String hubUserId) {
		this.hubUserId = hubUserId;
	}

	public String getHubUserPassword() {
		return hubUserPassword;
	}

	public void setHubUserPassword(String hubUserPassword) {
		this.hubUserPassword = hubUserPassword;
	}

	public String getOrsId() {
		return orsId;
	}

	public void setOrsId(String orsId) {
		this.orsId = orsId;
	}

	public String getHubPingEndpoint() {
		return hubPingEndpoint;
	}

	public void setHubPingEndpoint(String hubPingEndpoint) {
		this.hubPingEndpoint = hubPingEndpoint;
	}

	public String getMqConnectionUser() {
		return mqConnectionUser;
	}

	public void setMqConnectionUser(String mqConnectionUser) {
		this.mqConnectionUser = mqConnectionUser;
	}

	public String getMqConnectionUserPassword() {
		return mqConnectionUserPassword;
	}

	public void setMqConnectionUserPassword(String mqConnectionUserPassword) {
		this.mqConnectionUserPassword = mqConnectionUserPassword;
	}

	public String getHl7ActiveEventCodes() {
		return hl7ActiveEventCodes;
	}

	public void setHl7ActiveEventCodes(String hl7ActiveEventCodes) {
		this.hl7ActiveEventCodes = hl7ActiveEventCodes;
	}

	public String getHl7PutEventCodes() {
		return hl7PutEventCodes;
	}

	public void setHl7PutEventCodes(String hl7EventCodes) {
		this.hl7PutEventCodes = hl7EventCodes;
	}

	public String getHl7MergeEventCodes() {
		return hl7MergeEventCodes;
	}

	public void setHl7MergeEventCodes(String hl7MergeEventCodes) {
		this.hl7MergeEventCodes = hl7MergeEventCodes;
	}

	public String getHl7UnmergeEventCodes() {
		return hl7UnmergeEventCodes;
	}

	public void setHl7UnmergeEventCodes(String hl7UnmergeEventCodes) {
		this.hl7UnmergeEventCodes = hl7UnmergeEventCodes;
	}

	public int getRetryIntervalSeconds() {
		return retryIntervalSeconds;
	}

	public void setRetryIntervalSeconds(int retryIntervalSeconds) {
		this.retryIntervalSeconds = retryIntervalSeconds;
	}

	public int getFailbackIntervalSeconds() {
		return failbackIntervalSeconds;
	}

	public void setFailbackIntervalSeconds(int failbackIntervalSeconds) {
		this.failbackIntervalSeconds = failbackIntervalSeconds;
	}

	public int getMaxRetryAttempts() {
		return maxRetryAttempts;
	}

	public void setMaxRetryAttempts(int maxRetryAttempts) {
		this.maxRetryAttempts = maxRetryAttempts;
	}

	public int getFailbackIntervalMilliseconds() {
		return failbackIntervalSeconds * 1000;
	}

	public String getSourceSystemPrefix() {
		return sourceSystemPrefix;
	}

	public void setSourceSystemPrefix(String sourceSystemPrefix) {
		this.sourceSystemPrefix = sourceSystemPrefix;
	}

	public int getErrorQueueMaxSuccessiveFailures() {
		return errorQueueMaxSuccessiveFailures;
	}

	public void setErrorQueueMaxSuccessiveFailures(int errorQueueMaxSuccessiveFailures) {
		this.errorQueueMaxSuccessiveFailures = errorQueueMaxSuccessiveFailures;
	}

	public static int getErrorQueueSuccessiveFailureCounter() {
		return errorQueueSuccessiveFailureCounter;
	}

	public static void setErrorQueueSuccessiveFailureCounter(int errorQueueSuccessiveFailureCounter) {
		ApplicationConfiguration.errorQueueSuccessiveFailureCounter = errorQueueSuccessiveFailureCounter;
	}

	public static void incrementQueueFailureCounter(){
		errorQueueSuccessiveFailureCounter++;
	}
}
