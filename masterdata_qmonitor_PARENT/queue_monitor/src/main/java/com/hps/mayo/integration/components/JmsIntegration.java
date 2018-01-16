package com.hps.mayo.integration.components;

import java.util.HashMap;

import javax.annotation.Resource;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.messaging.MessageHeaders;

import com.hps.mayo.application.utilities.StringUtilities;
import com.hps.mayo.exceptions.ResourceUnavailableException;

public class JmsIntegration {

	private final Logger logger = LogManager.getLogger(this.getClass());

	@Autowired
	@Resource(name = "jmsTemplatePrimary")
	JmsTemplate jmsTemplatePrimary;
	@Autowired
	@Resource(name = "jmsTemplateSecondary")
	JmsTemplate jmsTemplateSecondary;
	@Autowired
	@Resource(name = "jmsExceptionDestination")
	Destination jmsExceptionDestination;

	public void sendToQueue(Destination jmsDestination, String payload, final MessageHeaders headerProps)
			throws RuntimeException {

		try {
			logger.debug("Publishing to JMS queue.\n\nMessage:\n\n" + payload);
			if (headerProps != null && !headerProps.isEmpty()) {
				jmsTemplatePrimary.convertAndSend(jmsDestination.toString(), payload, new MessagePostProcessor() {
					public Message postProcessMessage(Message message) throws JMSException {
						logger.debug("Header Properties:");
						for (String key : headerProps.keySet()) {
							logger.debug("Key : " + key + " Value: " + headerProps.get(key));
							if (headerProps.get(key) instanceof String)
								message.setStringProperty(key, (String) headerProps.get(key));
							if (headerProps.get(key) instanceof Long)
								message.setStringProperty(key, Long.toString((Long) headerProps.get(key)));
						}
						return message;
					}
				});
			} else {
				jmsTemplatePrimary.convertAndSend(jmsDestination.toString(), payload);
			}
			logger.info("Completed publication to JMS queue.");
		} catch (Exception e) {

			logger.error("Error publishing to queue:" + jmsDestination.toString());
			logger.error(e.getMessage());
			logger.error(StringUtilities.stackTraceToString(e.getStackTrace()));

			logger.error("Attempt to publish using backup Queue Manager");
			try {
				logger.debug("Publishing to JMS queue.\n\nMessage:\n\n" + payload);
				if (headerProps != null && !headerProps.isEmpty()) {
					jmsTemplateSecondary.convertAndSend(jmsDestination.toString(), payload, new MessagePostProcessor() {
						public Message postProcessMessage(Message message) throws JMSException {
							logger.debug("Header Properties:");
							for (String key : headerProps.keySet()) {
								logger.debug("Key : " + key + " Value: " + headerProps.get(key));
								if (headerProps.get(key) instanceof String)
									message.setStringProperty(key, (String) headerProps.get(key));
								if (headerProps.get(key) instanceof Long)
									message.setStringProperty(key, Long.toString((Long) headerProps.get(key)));
							}
							return message;
						}
					});
				} else {
					jmsTemplateSecondary.convertAndSend(jmsDestination.toString(), payload);
				}
				logger.info("Completed publication to Backup JMS queue.");
			} catch (Exception e2) {
				throw new ResourceUnavailableException(
						"Error publishing to queue using bakup Queue Manager with Destination:"
								+ jmsDestination.toString() + " Error:" + e2.getMessage());
			}
		}

	}
}
