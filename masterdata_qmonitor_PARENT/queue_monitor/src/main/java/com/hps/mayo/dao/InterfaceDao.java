package com.hps.mayo.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hps.mayo.businessobjects.MdmInterfaceMessage;
import com.hps.mayo.configuration.ApplicationConfiguration;
import com.hps.mayo.integration.client.BusinessEntityServiceClient;
import com.hps.mayo.services.HttpService;

import edu.mayo.mdm.party.services.partyservicedata.UserCredentials;
import edu.mayo.mdm.party.services.partyservicemessages.HubPingRequest;
import edu.mayo.mdm.party.services.partyservicemessages.HubPingResponse;
import edu.mayo.mdm.party.services.partyservicemessages.MergePartyRequest;
import edu.mayo.mdm.party.services.partyservicemessages.MergePartyResponse;
import edu.mayo.mdm.party.services.partyservicemessages.PutPartyRequest;
import edu.mayo.mdm.party.services.partyservicemessages.PutPartyResponse;
import edu.mayo.mdm.party.services.partyservicemessages.UnmergePartyRequest;
import edu.mayo.mdm.party.services.partyservicemessages.UnmergePartyResponse;

@Service
public class InterfaceDao {

	private Logger logger = LogManager.getLogger(this.getClass());
	private HubPingRequest hubPingRequest;

	@Autowired
	HttpService httpService;
	@Autowired
	ApplicationConfiguration appConfig;
	@Autowired
	BusinessEntityServiceClient bes;

	public MdmInterfaceMessage putParty(MdmInterfaceMessage message, boolean nullOutMissingFields) throws Exception {

		if (logger.isDebugEnabled())
			logger.debug("-------- BEGIN InterfaceDao.putParty --------");

		// Call the service and get the response object in the business object.
		PutPartyRequest putPartyRequest = message.getPutPartyRequest(nullOutMissingFields);
		putPartyRequest.setUserCredentials(getUserCredentials());

		if (logger.isDebugEnabled()) {
			ObjectMapper mapper = new ObjectMapper();
			logger.debug("PutPartyRequest:\n" + mapper.writeValueAsString(putPartyRequest) + "\n");
		}

		PutPartyResponse response = bes.executePutRequest(putPartyRequest);
		message.setPutPartyResponse(response);

		return message;

	}

	public MdmInterfaceMessage mergeParty(MdmInterfaceMessage message) throws Exception {

		if (logger.isDebugEnabled())
			logger.debug("-------- BEGIN InterfaceDao.mergeParty --------");

		// Call the service and get the response object in the business object.
		MergePartyRequest mergePartyRequest = message.getMergePartyRequest();
		if (mergePartyRequest != null) {

			mergePartyRequest.setUserCredentials(getUserCredentials());

			if (logger.isDebugEnabled()) {
				ObjectMapper mapper = new ObjectMapper();
				logger.debug("MergePartyRequest:\n" + mapper.writeValueAsString(mergePartyRequest) + "\n");
			}

			MergePartyResponse response = bes.executeMergeRequest(mergePartyRequest);
			message.setMergePartyResponse(response);

		} else {
			message.setMergePartyResponse(null);
		}

		return message;

	}

	public MdmInterfaceMessage unmergeParty(MdmInterfaceMessage message) throws Exception {

		if (logger.isDebugEnabled())
			logger.debug("-------- BEGIN InterfaceDao.unmergeParty --------");

		// Call the service and get the response object in the business object.
		UnmergePartyRequest unmergePartyRequest = message.getUnmergePartyRequest();
		if (unmergePartyRequest != null) {

			unmergePartyRequest.setUserCredentials(getUserCredentials());

			if (logger.isDebugEnabled()) {
				ObjectMapper mapper = new ObjectMapper();
				logger.debug("UnmergePartyRequest:\n" + mapper.writeValueAsString(unmergePartyRequest) + "\n");
			}

			UnmergePartyResponse response = bes.executeUnMergeRequest(unmergePartyRequest);
			message.setUnmergePartyResponse(response);

		} else {
			message.setUnmergePartyResponse(null);
		}

		return message;

	}

	public HubPingResponse pingMdmServer() throws Exception {

		if (hubPingRequest == null)
			hubPingRequest = new HubPingRequest();
		hubPingRequest.setUserCredentials(getUserCredentials());
		hubPingRequest.setInputBuffer("?");

		HubPingResponse response = bes.executePingHubRequest(httpService.getPingUri().toURI(), hubPingRequest);

		return response;

	}

	public UserCredentials getUserCredentials() {
		UserCredentials userCredentials = new UserCredentials();
		String username = appConfig.getHubUserId();
		String pwd = appConfig.getHubUserPassword();
		userCredentials.setUserName(username);
		userCredentials.setPassword(pwd);
		return userCredentials;
	}

}
