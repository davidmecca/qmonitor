package com.hps.mayo.message.transformers;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hl7.v2xml.HL7;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.Transformer;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.SimpleMessageConverter;
import org.springframework.messaging.Message;
import org.springframework.util.ObjectUtils;

import com.hps.mayo.exceptions.MessageParsingException;
import com.hps.mayo.services.XmlServices;

import edu.mayo.mdm.util.HL7Segments;
import edu.mayo.mdm.util.StringCtrlStripper;

public class MessageTransformer extends SimpleMessageConverter {

	private Logger logger = LogManager.getLogger(this.getClass());
	private StringCtrlStripper stringStripper = new StringCtrlStripper();

	@Autowired
	XmlServices xmlServices;

	@Transformer
	public String transformPayload(Message message) throws Exception {
		if (message instanceof TextMessage) {
			logger.debug("In transformation.  TextMessage payload instance.");
			return stringStripper.strip(extractStringFromMessage((TextMessage) message));
		} else if (message instanceof BytesMessage) {
			logger.debug("In transformation.  BytesMessage payload instance.");
			return stringStripper.strip(extractByteArrayFromMessageAsString((BytesMessage) message));
		} else {
			throw new MessageConversionException(
					"Unexpected Message Type received. " + "Allowed types are TextMessage and BytesMessage. "
							+ "Received JMS type: " + ObjectUtils.nullSafeClassName(message));
		}
	}

	@Transformer
	public String transformPayload(String message) throws Exception {
		logger.debug("In transformation.  String payload instance.");
		return stringStripper.strip(message);
	}

	@Transformer
	public String transformPayload(byte[] message) throws Exception {
		logger.debug("In transformation.  Byte[] payload instance.");
		return stringStripper.strip(new String(message));
	}

	public JAXBElement<HL7> parseHl7(String payload) throws JAXBException {

		logger.debug("Start of parsing HL7 payload.");
		try {
			return xmlServices.parseHL7Payload(payload);
		} catch (JAXBException e) {
			logger.error(e.getMessage());
			throw new MessageParsingException(e);
		}

	}

	public Message confirmMessageElements(Message message) {

		JAXBElement jaxbHl7 = (JAXBElement) message.getPayload();
		HL7Segments hl7Segments = new HL7Segments(jaxbHl7);
		String hl7MessageControlId = hl7Segments.getMsh().getMSH10MessageControlID();
		if (logger.isDebugEnabled())
			logger.debug("Confirming required HL7 element(s) messageControlID = " + hl7MessageControlId);

		if (hl7Segments.getEvn() == null) {
			logger.error("Invalid HL7 content for message messageControlID = " + hl7MessageControlId);
			throw new MessageParsingException("Nul/missing ENV node in message.  Invalid HL7 content for message messageControlID = " + hl7MessageControlId);
		}

		return message;

	}

	private String extractByteArrayFromMessageAsString(BytesMessage message) throws JMSException {
		return message.readUTF();
	}

}