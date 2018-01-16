package com.hps.mayo.integration.messaging;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.jms.Destination;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.ErrorMessage;

import com.hps.mayo.application.utilities.CommonUtils;
import com.hps.mayo.configuration.AppConstants;
import com.hps.mayo.integration.components.JmsIntegration;

public class ErrorChannelHandler {

	private Logger logger = LogManager.getLogger(this.getClass());

	@Autowired
	@Qualifier("jmsIntegration")
	JmsIntegration jmsIntegration;

	@Resource(name = "jmsExceptionDestination")
	Destination jmsExceptionDestination;

	public void onMessage(ErrorMessage errorMessage) throws RuntimeException {

		String logMsg = "MDM_QUEUE_MONITOR: Exception with interface message.  Error cause:"
				+ errorMessage.getPayload().getCause() + AppConstants.NEW_LINE + " Payload:" + errorMessage.getPayload().getMessage();
		CommonUtils.log2Tivoli(logMsg, "MDM_QUEUE_MONITOR", "Exception", "ERROR", "", null);
		
		logger.error("Sending payload to exception queue.  Payload error: " + errorMessage.getPayload().getMessage());
		
		Map<String, Object> headerProps = new HashMap<String, Object>();
		headerProps.put("ERROR_MESSAGE", errorMessage.getPayload().getCause());

		jmsIntegration.sendToQueue(jmsExceptionDestination, errorMessage.getPayload().getMessage(),
				new MessageHeaders(headerProps));

	}

}
