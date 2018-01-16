package com.hps.mayo.services;

import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hl7.v2xml.HL7;
import org.springframework.stereotype.Service;

@Service
public class XmlServices {

	private Logger logger = LogManager.getLogger(this.getClass());

	private JAXBContext jaxContext;
	private JAXBElement<HL7> root = null;
	private Unmarshaller unmarshaller = null;

	public XmlServices() throws Exception {

		jaxContext = JAXBContext.newInstance(HL7.class);
		unmarshaller = jaxContext.createUnmarshaller();

	}

	public XmlServices(String xsdClass) throws Exception {

		jaxContext = JAXBContext.newInstance(xsdClass);
		unmarshaller = jaxContext.createUnmarshaller();

	}

	public JAXBElement<HL7> parseHL7Payload(String payload) throws JAXBException {

		try {
			if (logger.isDebugEnabled())
				logger.debug("Parsing message:\n" + payload);
			if (jaxContext == null) {
				if (logger.isDebugEnabled())
					logger.debug("Creating new unmarshaller");
				this.jaxContext = JAXBContext.newInstance(HL7.class);
				this.unmarshaller = jaxContext.createUnmarshaller();
			}
			unmarshaller.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());
			root = this.unmarshaller.unmarshal(new StreamSource(new StringReader(payload)), HL7.class);
		} catch (JAXBException e) {
			logger.error("HL7 parsing error: ", e);
			throw e;
		}

		return root;
	}

}
