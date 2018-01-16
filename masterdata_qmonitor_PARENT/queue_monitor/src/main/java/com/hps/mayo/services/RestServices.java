package com.hps.mayo.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hps.mayo.businessobjects.MdmInterfaceMessage;
import com.hps.mayo.dao.InterfaceDao;
import com.hps.mayo.exceptions.MdmProcessingException;
import com.hps.mayo.exceptions.MessageParsingException;

import edu.mayo.mdm.party.services.partyservicemessages.HubPingResponse;
import edu.mayo.mdm.util.HL7Segments;

@Service
public class RestServices {

	private Logger logger = LogManager.getLogger(this.getClass());

	@Autowired
	private InterfaceDao interfaceDao;

	public RestServices() {

	}

	public MdmInterfaceMessage putMdmParty(HL7Segments hl7Segments, boolean nullOutMissingFields) throws Exception {

		MdmInterfaceMessage interfaceMessage = null;
		interfaceMessage = new MdmInterfaceMessage(hl7Segments);

		if (logger.isDebugEnabled())
			logger.debug("-------- BEGIN RestServices.putMdmParty --------");

		interfaceDao.putParty(interfaceMessage, nullOutMissingFields);

		if (logger.isDebugEnabled())
			logger.debug("Put party response: " + interfaceMessage.getPutPartyResponse().getOverallStatus());

		return interfaceMessage;

	}

	public MdmInterfaceMessage mergeMdmParty(HL7Segments hl7Segments) throws Exception {

		MdmInterfaceMessage interfaceMessage = new MdmInterfaceMessage(hl7Segments);

		if (logger.isDebugEnabled())
			logger.debug("-------- BEGIN RestServices.mergeMdmParty --------");

		interfaceDao.mergeParty(interfaceMessage);

		if (logger.isDebugEnabled()) {
			if (interfaceMessage.getMergePartyResponse() != null)
				logger.debug("Merge party response: " + interfaceMessage.getMergePartyResponse().getOverallStatus());
		}
		return interfaceMessage;

	}

	public MdmInterfaceMessage unmergeMdmParty(HL7Segments hl7Segments) throws Exception {

		String outMsg = "";

		MdmInterfaceMessage interfaceMessage = new MdmInterfaceMessage(hl7Segments);

		if (logger.isDebugEnabled()) {
			outMsg = "-------- BEGIN RestServices.unmergeMdmParty --------";
			logger.debug(outMsg);
		}

		interfaceDao.unmergeParty(interfaceMessage);

		if (logger.isDebugEnabled())
			logger.debug("Unmerge party response: "
					+ interfaceMessage.getUnmergePartyResponse().getResponseStatus().getMessage());

		return interfaceMessage;

	}

	public String pingMdmHubServer() throws Exception {

		HubPingResponse response = interfaceDao.pingMdmServer();

		if (response != null)
			return response.getResponseStatus().getCode();

		return null;

	}

}
