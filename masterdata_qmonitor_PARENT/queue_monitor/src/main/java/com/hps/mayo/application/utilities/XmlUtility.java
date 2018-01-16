package com.hps.mayo.application.utilities;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import org.hl7.v2xml.HL7;
import org.springframework.util.StringUtils;

import com.hps.mayo.configuration.AppConstants;

public class XmlUtility {
	
	private static JAXBContext jc = null;
	
	public XmlUtility(){
		try {
			jc = JAXBContext.newInstance(HL7.class);
		} catch (JAXBException e) {
			e.printStackTrace();
		} 
	}
	
	public static String toXmlString(JAXBElement element) {
	    try {
	        Marshaller marshaller = jc.createMarshaller();  
	        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);  
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        marshaller.marshal(element, baos);
	        return baos.toString();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }      
	    return AppConstants.EMPTY_STRING;
	}
	


}
