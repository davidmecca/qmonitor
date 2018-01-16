package com.hps.mayo.integration.client;

import java.net.URI;

import javax.annotation.PostConstruct;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.hps.mayo.application.utilities.CommonUtils;
import com.hps.mayo.configuration.ApplicationConfiguration;
import com.hps.mayo.exceptions.MdmProcessingException;
import com.hps.mayo.services.HttpErrorHandlerServices;
import com.hps.mayo.services.HttpService;

import edu.mayo.mdm.party.services.partyservicemessages.HubPingRequest;
import edu.mayo.mdm.party.services.partyservicemessages.HubPingResponse;
import edu.mayo.mdm.party.services.partyservicemessages.MergePartyRequest;
import edu.mayo.mdm.party.services.partyservicemessages.MergePartyResponse;
import edu.mayo.mdm.party.services.partyservicemessages.PutPartyRequest;
import edu.mayo.mdm.party.services.partyservicemessages.PutPartyResponse;
import edu.mayo.mdm.party.services.partyservicemessages.UnmergePartyRequest;
import edu.mayo.mdm.party.services.partyservicemessages.UnmergePartyResponse;

@Component
@EnableRetry
public class BusinessEntityServiceClient {

	Logger logger = LoggerFactory.getLogger(BusinessEntityServiceClient.class);

	private HttpHeaders reqHeaders = new HttpHeaders();
	private RestTemplate restTemplate = null;
	private HttpStatus httpStatus = null;

	@Autowired
	ApplicationConfiguration appConfig;
	@Autowired
	HttpService httpService;
	@Autowired
	HttpErrorHandlerServices httpErrorHandler;

	@PostConstruct
	public void init() {

		reqHeaders.add(HttpHeaders.COOKIE, httpService.getAuthHashCookie());
		restTemplate = httpService.getRestTemplate();

	}

	@Retryable(maxAttemptsExpression = "#{${max.retry.attempts}}", backoff = @Backoff(delay = 5000), value = {
			HttpClientErrorException.class, ResourceAccessException.class, HttpServerErrorException.class,
			RestClientException.class, MdmProcessingException.class })
	public PutPartyResponse executePutRequest(PutPartyRequest putPartyRequest) {

		URI uri = null;

		try {
			httpErrorHandler.revertToPrimaryNode();
			uri = httpService.getPostUri().toURI();
			HttpEntity<PutPartyRequest> request = new HttpEntity<>(putPartyRequest);
			PutPartyResponse response = restTemplate.postForObject(uri, request, PutPartyResponse.class);

			if (logger.isDebugEnabled()) {
				logger.debug("Put response code:" + response.getOverallStatus());
				logger.debug(response.getPartyResponseStatus().toString());
			}

			/*
			 * Trapping condition if MDM is down or throwing severe error but
			 * the party services app is running
			 * 
			 * This will force a retry of the message on an alternate node
			 * 
			 */
			if (response.returnedForceRetryException()) {
				String logMsg = "Forcing retry due to severe MDM error.  Error:" + response;
				logger.error(logMsg);
				CommonUtils.log2Tivoli(logMsg, "MDM_QUEUE_MONITOR", "Exception", "ERROR", "", null);
				throw new MdmProcessingException();
			}

			return response;
		
			/*
			 * Trapping exception if the party services app is down.
			 */
		} catch (HttpClientErrorException e) {
			httpStatus = e.getStatusCode();
			logger.error("Http Status Code:" + httpStatus + " -- Http Error: " + e.getResponseBodyAsString()
					+ "\n\nWill switch to alternate node and retry.");
			if (httpStatus == HttpStatus.NOT_FOUND) {
				httpErrorHandler.switchToAlternateNode();
				throw e; // Throw to force a retry
			}

			/*
			 * ResourceAccessException - This traps a security error if client
			 * doesn't have cert to access a SSL port. Should not occur but
			 * trapping it regardless
			 * 
			 */
		} catch (ResourceAccessException e) {
			logger.error("Fatal security access exception for URI: " + uri + "\n\nError:" + e.getMessage());
			logger.error("Http Error: " + e.getMessage() + "\n\nWill switch to alternate node and retry.");
			httpErrorHandler.switchToAlternateNode();
			throw e; // Throw to force a retry
		} catch (HttpServerErrorException e) {
			logger.error("HTTP SERVER ERR - " + e.getMessage());
			try {
				JSONObject errorResponse = (new JSONObject(e.getResponseBodyAsString()));
				logger.error(errorResponse.getString("errorCode") + ":" + errorResponse.getString("errorMessage"));

			} catch (JSONException jse) {
				jse.printStackTrace();

			}
			httpErrorHandler.switchToAlternateNode();
			throw e;

		} catch (RestClientException e) {
			e.printStackTrace();
			logger.debug(e.getMessage());
			logger.debug(e.getLocalizedMessage());
			httpErrorHandler.switchToAlternateNode();
			throw e;

		} catch (MdmProcessingException e) {
			httpErrorHandler.switchToAlternateNode();
			throw e;
		} catch (Exception e) {
			logger.error("General Rest Template exception: " + e.getMessage());
		}

		return null;

	}

	@Retryable(maxAttemptsExpression = "#{${max.retry.attempts}}", backoff = @Backoff(delay = 5000), value = {
			HttpClientErrorException.class, ResourceAccessException.class, HttpServerErrorException.class,
			RestClientException.class, MdmProcessingException.class })
	public MergePartyResponse executeMergeRequest(MergePartyRequest mergePartyRequest) {

		URI uri = null;
		try {
			httpErrorHandler.revertToPrimaryNode();
			uri = httpService.getMergeUri().toURI();
			HttpEntity<MergePartyRequest> request = new HttpEntity<>(mergePartyRequest);
			MergePartyResponse response = restTemplate.postForObject(uri, request, MergePartyResponse.class);

			if (logger.isDebugEnabled()) {
				logger.debug("Merge response code:" + response.getOverallStatus());
			}

			if (response.returnedForceRetryException()) {
				String logMsg = "Forcing retry due to severe MDM error.  Error:" + response;
				logger.error(logMsg);
				CommonUtils.log2Tivoli(logMsg, "MDM_QUEUE_MONITOR", "Exception", "ERROR", "", null);
				throw new MdmProcessingException();
			}

			return response;

		} catch (HttpClientErrorException e) {
			httpStatus = e.getStatusCode();
			logger.error("Http Status Code:" + httpStatus + " -- Http Error: " + e.getResponseBodyAsString()
					+ "\n\nWill switch to alternate node and retry.");
			if (httpStatus == HttpStatus.NOT_FOUND) {
				httpErrorHandler.switchToAlternateNode();
				throw e; // Throw to force a retry
			}

			/*
			 * ResourceAccessException - This traps a security error if client
			 * doesn't have cert to access a SSL port. Should not occur but
			 * trapping it regardless
			 * 
			 */
		} catch (ResourceAccessException e) {
			logger.error("Fatal security access exception for URI: " + uri + "\n\nError:" + e.getMessage());
			logger.error("Http Error: " + e.getMessage() + "\n\nWill switch to alternate node and retry.");
			httpErrorHandler.switchToAlternateNode();
			throw e; // Throw to force a retry
		} catch (HttpServerErrorException e) {
			logger.error("HTTP SERVER ERR - " + e.getMessage());
			try {
				JSONObject errorResponse = (new JSONObject(e.getResponseBodyAsString()));
				logger.error(errorResponse.getString("errorCode") + ":" + errorResponse.getString("errorMessage"));

			} catch (JSONException jse) {
				jse.printStackTrace();

			}
			httpErrorHandler.switchToAlternateNode();
			throw e;

		} catch (RestClientException e) {
			e.printStackTrace();
			logger.debug(e.getMessage());
			logger.debug(e.getLocalizedMessage());
			httpErrorHandler.switchToAlternateNode();
			throw e;

		} catch (MdmProcessingException e) {
			httpErrorHandler.switchToAlternateNode();
			throw e;
		} catch (Exception e) {
			logger.error("General Rest Template exception: " + e.getMessage());
		}

		return null;

	}

	@Retryable(maxAttemptsExpression = "#{${max.retry.attempts}}", backoff = @Backoff(delay = 5000), value = {
			HttpClientErrorException.class, ResourceAccessException.class, HttpServerErrorException.class,
			RestClientException.class, MdmProcessingException.class })
	public UnmergePartyResponse executeUnMergeRequest(UnmergePartyRequest unmergePartyRequest) {

		URI uri = null;
		try {

			httpErrorHandler.revertToPrimaryNode();
			uri = httpService.getUnmergeUri().toURI();
			HttpEntity<UnmergePartyRequest> request = new HttpEntity<>(unmergePartyRequest);
			UnmergePartyResponse response = restTemplate.postForObject(uri, request, UnmergePartyResponse.class);

			if (logger.isDebugEnabled()) {
				logger.debug("UnMerge response code:" + response.getOverallStatus());
			}

			if (response.returnedForceRetryException()) {
				String logMsg = "Forcing retry due to severe MDM error.  Error:" + response;
				logger.error(logMsg);
				CommonUtils.log2Tivoli(logMsg, "MDM_QUEUE_MONITOR", "Exception", "ERROR", "", null);
				throw new MdmProcessingException();
			}

			return response;

		} catch (HttpClientErrorException e) {
			httpStatus = e.getStatusCode();
			logger.error("Http Status Code:" + httpStatus + " -- Http Error: " + e.getResponseBodyAsString()
					+ "\n\nWill switch to alternate node and retry.");
			if (httpStatus == HttpStatus.NOT_FOUND) {
				httpErrorHandler.switchToAlternateNode();
				throw e; // Throw to force a retry
			}

			/*
			 * ResourceAccessException - This traps a security error if client
			 * doesn't have cert to access a SSL port. Should not occur but
			 * trapping it regardless
			 * 
			 */
		} catch (ResourceAccessException e) {
			logger.error("Fatal security access exception for URI: " + uri + "\n\nError:" + e.getMessage());
			logger.error("Http Error: " + e.getMessage() + "\n\nWill switch to alternate node and retry.");
			httpErrorHandler.switchToAlternateNode();
			throw e; // Throw to force a retry
		} catch (HttpServerErrorException e) {
			logger.error("HTTP SERVER ERR - " + e.getMessage());
			try {
				JSONObject errorResponse = (new JSONObject(e.getResponseBodyAsString()));
				logger.error(errorResponse.getString("errorCode") + ":" + errorResponse.getString("errorMessage"));

			} catch (JSONException jse) {
				jse.printStackTrace();

			}
			httpErrorHandler.switchToAlternateNode();
			throw e;

		} catch (RestClientException e) {
			e.printStackTrace();
			logger.debug(e.getMessage());
			logger.debug(e.getLocalizedMessage());
			httpErrorHandler.switchToAlternateNode();
			throw e;

		} catch (MdmProcessingException e) {
			httpErrorHandler.switchToAlternateNode();
			throw e;
		} catch (Exception e) {
			logger.error("General Rest Template exception: " + e.getMessage());
		}

		return null;

	}

	public HubPingResponse executePingHubRequest(URI uri, HubPingRequest hubPingRequest) {

		try {

			HttpEntity<HubPingRequest> request = new HttpEntity<>(hubPingRequest);
			HubPingResponse response = restTemplate.postForObject(uri, request, HubPingResponse.class);

			if (logger.isDebugEnabled()) {
				logger.debug("Put response code:" + response.getResponseStatus());
			}

			return response;

		} catch (HttpClientErrorException e) {
			logger.error("HTTP CLIENT EXCEPTION - " + e.getResponseBodyAsString());

		} catch (HttpServerErrorException e) {
			logger.error("HTTP SERVER ERR - " + e.getMessage());
			try {
				JSONObject errorResponse = (new JSONObject(e.getResponseBodyAsString()));
				logger.error(errorResponse.getString("errorCode") + ":" + errorResponse.getString("errorMessage"));

			} catch (JSONException jse) {
				jse.printStackTrace();

			}

		} catch (RestClientException e) {
			e.printStackTrace();
			logger.debug(e.getMessage());
			logger.debug(e.getLocalizedMessage());

		} catch (Exception e) {
			logger.error("General Rest Template exception: " + e.getMessage());
		}

		return null;

	}

}
