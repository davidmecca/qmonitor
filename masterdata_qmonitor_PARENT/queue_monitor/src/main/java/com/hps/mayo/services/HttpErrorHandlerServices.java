package com.hps.mayo.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hps.mayo.configuration.ApplicationConfiguration;

@Service
public class HttpErrorHandlerServices {

	private Logger logger = LogManager.getLogger(this.getClass());
	@Autowired
	ApplicationConfiguration appConfig;
	@Autowired
	HttpService httpService;
	@Autowired
	RestServices restServices;

	public boolean revertToPrimaryNode() {
		boolean result = false;
		long currentTimeInMilliseconds = System.currentTimeMillis();
		if (logger.isDebugEnabled()) {
			logger.debug("Reverting back to primary node.");
			logger.debug("Failback interval: " + appConfig
					.getFailbackIntervalMilliseconds());
			logger.debug("Time since last failover:"
					+ (currentTimeInMilliseconds - appConfig.getLastFailoverStartTimeInMilliseconds()));

		}
		if (appConfig.getCurrentTargetServicesNode() == 2
				&& (currentTimeInMilliseconds - appConfig.getLastFailoverStartTimeInMilliseconds()) > appConfig
						.getFailbackIntervalMilliseconds()) {
			
			if(logger.isDebugEnabled()){
				logger.debug("Executing switch to alternate node.");
			}
			switchToAlternateNode(); // Switch back to the primary
										// node
			if (executePing()) {
				logger.info("Reverting back to party services primary node.  Node:"
						+ appConfig.getCurrentTargetServicesNode());
				result = true;
			} else {
				// Ping failed so switch back to alternate node
				switchToAlternateNode();
			}

			appConfig.setLastFailoverStartTimeInMilliseconds(System.currentTimeMillis());
		}
		return result;
	}

	public boolean executePing() {

		String httpStatusCode = null;

		try {
			httpStatusCode = restServices.pingMdmHubServer();
		} catch (Exception e) {
			logger.error("Error with ping request on MDM hub node: " + appConfig.getCurrentTargetServicesNode());
		}

		if (!"200".equalsIgnoreCase(httpStatusCode))
			return false;

		return true;

	}

	public void switchToAlternateNode() {

		if (appConfig.getNumberOfNodes() > 1) {
			if (appConfig.getCurrentTargetServicesNode() == 1) {
				appConfig.setActiveEndPoint(appConfig.getNode2EndPoint());
				appConfig.setCurrentTargetServicesNode(2);
			} else {
				appConfig.setActiveEndPoint(appConfig.getNode1EndPoint());
				appConfig.setCurrentTargetServicesNode(1);
			}
		}

		logger.error("In node retry.  Processing on node:" + appConfig.getCurrentTargetServicesNode());

		appConfig.setLastFailoverStartTimeInMilliseconds(System.currentTimeMillis());

	}

}
