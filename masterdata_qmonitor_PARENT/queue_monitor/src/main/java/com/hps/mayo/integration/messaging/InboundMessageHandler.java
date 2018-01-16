package com.hps.mayo.integration.messaging;

import javax.annotation.Resource;
import javax.jms.Destination;
import javax.xml.bind.JAXBElement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hl7.v2xml.EVNCONTENT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.Message;
import org.springframework.transaction.annotation.Transactional;

import com.hps.mayo.application.utilities.CommonUtils;
import com.hps.mayo.application.utilities.XmlUtility;
import com.hps.mayo.businessobjects.MdmInterfaceMessage;
import com.hps.mayo.configuration.AppConstants;
import com.hps.mayo.configuration.ApplicationConfiguration;
import com.hps.mayo.configuration.Hl7CodesMappingConfig;
import com.hps.mayo.exceptions.MessageProcessingException;
import com.hps.mayo.integration.components.JmsIntegration;
import com.hps.mayo.message.enricher.HeaderEnricher;
import com.hps.mayo.services.HttpErrorHandlerServices;
import com.hps.mayo.services.RestServices;

import edu.mayo.mdm.util.Constants;
import edu.mayo.mdm.util.HL7Segments;
import edu.mayo.mdm.util.StopWatch;

@SuppressWarnings("static-access")
public class InboundMessageHandler {

	private Logger logger = LogManager.getLogger(this.getClass());
	private static Logger tivolilog = LogManager.getLogger("MpiiInterfaceTivoli");

	private String hl7EventCode = AppConstants.UNDETERMINED;
	private String hl7EventCodeDesc = AppConstants.UNDETERMINED;
	private String hl7MessageControlId = AppConstants.UNDETERMINED;
	private HL7Segments hl7Segments;
	private StringBuilder logMessageBuilder = new StringBuilder();
	private StopWatch stopWatch = null; // Used for logging execution duration
	private XmlUtility xmlUtility = new XmlUtility();

	@Resource(name = "jmsExceptionDestination")
	Destination jmsExceptionDestination;

	@Autowired
	@Qualifier("jmsIntegration")
	JmsIntegration jmsIntegration;

	@Autowired
	@Qualifier("hl7PropertiesBean")
	Hl7CodesMappingConfig hl7MappingConfig;

	@Autowired
	ApplicationConfiguration appConfig;

	@Autowired
	HttpErrorHandlerServices messageHandlerServices;

	@Autowired
	RestServices restClient;

	@Transactional
	public void onMessage(Message payload) throws Exception {

		if (logger.isDebugEnabled()) {
			stopWatch = new StopWatch();
			logger.debug("MDM Queue Monitor processing new message.  Message:\n\n" + payload);
		}

		boolean errorFound = false;
		MdmInterfaceMessage interfaceMessage = new MdmInterfaceMessage(appConfig);
		JAXBElement jaxbHl7 = (JAXBElement) payload.getPayload();
		hl7Segments = new HL7Segments(jaxbHl7);
		hl7MessageControlId = hl7Segments.getMsh().getMSH10MessageControlID();
		logger.info("Processing MSG for messageControlID = " + hl7MessageControlId);

		EVNCONTENT evn = hl7Segments.getEvn();
		hl7EventCode = evn.getEVN1EventTypeCode();
		hl7EventCodeDesc = null;
		if (logger.isDebugEnabled())
			hl7EventCodeDesc = hl7MappingConfig.getCodeValueByKey(hl7EventCode);

		// Check if the message HL7 event is one configured to be sent to
		// MDM
		try {
			if (appConfig.getHl7ActiveEventCodes().contains(hl7EventCode)) {
				if (logger.isDebugEnabled()) {
					logMessageBuilder.append(Constants.LOG_FILE_DELIMITER);
					logMessageBuilder.append("EVENT");
					logMessageBuilder.append(Constants.LOG_FILE_DELIMITER);
					logMessageBuilder.append(hl7EventCode);
					logMessageBuilder.append(Constants.LOG_FILE_DELIMITER);
					logMessageBuilder.append(hl7EventCodeDesc);
					logMessageBuilder.append(Constants.LOG_FILE_DELIMITER);
					logMessageBuilder.append(stopWatch);
					logMessageBuilder.append(Constants.LOG_FILE_DELIMITER);
					logMessageBuilder.append("Sending to MDM interface");
					logMessageBuilder.append(Constants.LOG_FILE_DELIMITER);
					logMessageBuilder.append("Source System ID:");
					logMessageBuilder.append(hl7Segments.getPid().getPID2PatientID().getCX1());
					logMessageBuilder.append(Constants.LOG_FILE_DELIMITER);
					logMessageBuilder.append("Message Control ID:" + hl7MessageControlId);
					logger.debug(logMessageBuilder.toString());
				}

				// Call party put service
				if (appConfig.getHl7PutEventCodes().contains(hl7EventCode)) {

					if ("A04|A40|A47".contains(hl7EventCode))
						interfaceMessage = restClient.putMdmParty(hl7Segments, true);
					else
						interfaceMessage = restClient.putMdmParty(hl7Segments, false);

					String responseStatus = interfaceMessage.getPutPartyResponse().getOverallStatus();
					logger.debug("Put response: " + responseStatus);
					if (responseStatus != null
							&& !AppConstants.EXECUTION_SUCCESS_IND.equalsIgnoreCase(responseStatus)) {

						logger.debug(interfaceMessage.getOverallProcessingResponseStatus());
						errorFound = true;
						sendToErrorQueue(payload, interfaceMessage.getOverallProcessingResponseStatus());

					}
				}
				// Call party merge service
				if (appConfig.getHl7MergeEventCodes().contains(hl7EventCode)) {

					interfaceMessage = restClient.mergeMdmParty(hl7Segments);
					// Send failed message with appended error to the configured
					// JMS
					// error queue for
					// manual review
					String responseStatus = interfaceMessage.getMergePartyResponse().getOverallStatus();
					if (logger.isDebugEnabled())
						logger.debug("Merge response: " + responseStatus);
					if (responseStatus != null
							&& !AppConstants.EXECUTION_SUCCESS_IND.equalsIgnoreCase(responseStatus)) {

						errorFound = true;
						sendToErrorQueue(payload,
								interfaceMessage.getMergePartyResponse().getResponseStatus().getMessage());

					}
				}

				// Call party unmerge service
				if (appConfig.getHl7UnmergeEventCodes().contains(hl7EventCode)) {
					interfaceMessage = restClient.unmergeMdmParty(hl7Segments);
					String responseStatus = interfaceMessage.getUnmergePartyResponse().getOverallStatus();
					// Send failed message with appended error to the configured
					// JMS
					// error queue for
					// manual review
					logger.debug("Unmerge response: " + responseStatus);
					if (responseStatus != null
							&& !AppConstants.EXECUTION_SUCCESS_IND.equalsIgnoreCase(responseStatus)) {

						errorFound = true;
						sendToErrorQueue(payload,
								interfaceMessage.getUnmergePartyResponse().getResponseStatus().getMessage());

					}
				}
			} else {
				if (logger.isDebugEnabled()) {
					logMessageBuilder.append(Constants.LOG_FILE_DELIMITER);
					logMessageBuilder.append("EVENT");
					logMessageBuilder.append(Constants.LOG_FILE_DELIMITER);
					logMessageBuilder.append(hl7EventCode);
					logMessageBuilder.append(Constants.LOG_FILE_DELIMITER);
					logMessageBuilder.append(hl7EventCodeDesc);
					logMessageBuilder.append(Constants.LOG_FILE_DELIMITER);
					logMessageBuilder.append(stopWatch);
					logMessageBuilder.append(Constants.LOG_FILE_DELIMITER);
					logMessageBuilder.append("IGNORED");
					logMessageBuilder.append(Constants.LOG_FILE_DELIMITER);
					logMessageBuilder.append("Source System ID:");
					logMessageBuilder.append(hl7Segments.getPid().getPID2PatientID().getCX1());
					logMessageBuilder.append(Constants.LOG_FILE_DELIMITER);
					logMessageBuilder.append("Message Control ID:" + hl7MessageControlId);
					logger.debug(logMessageBuilder.toString());
				}

			}

		} catch (MessageProcessingException mpe) {
			logger.error("Generic processing exception encountered: " + mpe.getMessage());
			errorFound = true;
			sendToErrorQueue(payload, mpe.getMessage());
		}

		if (!errorFound) {
			if (logger.isDebugEnabled())
				logger.debug("Resetting error queue send counter to zero.");
			appConfig.setErrorQueueSuccessiveFailureCounter(0);
			logger.info("Completed processing MSG for messageControlID = " + hl7MessageControlId
					+ " -- Source System ID = " + hl7Segments.getPid().getPID2PatientID().getCX1());
		} else {
			logger.error("Error encountered for messageControlID = " + hl7MessageControlId + " -- Source System ID = "
					+ hl7Segments.getPid().getPID2PatientID().getCX1() + " -- Message sent to error queue.");
		}

	}

	private void sendToErrorQueue(Message payload, String message) {

		if (appConfig.getErrorQueueSuccessiveFailureCounter() <= appConfig.getErrorQueueMaxSuccessiveFailures()) {

			appConfig.incrementQueueFailureCounter();

			sendToQueue(payload, message);

		} else {
			String msg = "Pausing sending messages to error queue since maximum successive errors reached.  Maximum successive failure value:"
					+ appConfig.getErrorQueueMaxSuccessiveFailures();
			CommonUtils.log2Tivoli(msg, "MDM_QUEUE_MONITOR", "Exception", "ERROR", "", null);
			logger.error(msg);
			throw new RuntimeException(); // Force failed message back into
											// queue
		}

	}

	private void sendToQueue(Message payload, String message) {

		String msg = "MDM_QUEUE_MONITOR: Exception with interface message.  Message Control Identifier:"
				+ hl7MessageControlId + " -- Error message:" + message;
		logger.error(msg);
		CommonUtils.log2Tivoli(msg, "MDM_QUEUE_MONITOR", "Exception", "ERROR", "", null);

		// Add the error response details to the payload header to
		// pass to JMS header
		Message errorMessage = HeaderEnricher.transform(payload, AppConstants.ERROR_KEY, message);
		jmsIntegration.sendToQueue(jmsExceptionDestination,
				xmlUtility.toXmlString((JAXBElement) errorMessage.getPayload()), errorMessage.getHeaders());

	}

}
