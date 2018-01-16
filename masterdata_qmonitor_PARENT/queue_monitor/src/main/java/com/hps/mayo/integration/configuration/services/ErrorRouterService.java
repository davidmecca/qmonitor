package com.hps.mayo.integration.configuration.services;

import javax.jms.Destination;
import javax.xml.bind.JAXBElement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import com.hps.mayo.application.utilities.XmlUtility;
import com.hps.mayo.configuration.AppConstants;
import com.hps.mayo.integration.components.JmsIntegration;
import com.hps.mayo.message.enricher.HeaderEnricher;

@Service("errorRouterService")
public class ErrorRouterService {

	private Logger logger = LogManager.getLogger(this.getClass());
	XmlUtility xmlUtility = new XmlUtility();

	@Autowired
	JmsIntegration jmsIntegration;
	@Autowired
	@Qualifier("jmsExceptionDestination")
	Destination jmsExceptionDestination;

	// Put message that failed schema validation on the error queue
	public String parsingExceptionHandler(Message message) {

		logger.error("Received parsing error message.\n\nMessage:" + message.getPayload());

		// Add the error response details to the payload header to
		// pass to JMS header
		Message errorMessage = HeaderEnricher.transform(message, AppConstants.ERROR_KEY,
				"Error parsing received XML message");
		jmsIntegration.sendToQueue(jmsExceptionDestination,
				xmlUtility.toXmlString((JAXBElement) errorMessage.getPayload()), errorMessage.getHeaders());

		return null;

	}
	
	public void mdmExceptionHandler(Message message) {

		logger.error("Received MDM error message.\n\nMessage:" + message.getPayload());
		logger.error("Forcing message back to inbound queue for retry.");

		throw new RuntimeException();//Force all messages back to queue.

	}

}
