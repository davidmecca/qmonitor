package com.hps.mayo.http.request;

import java.net.URI;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.hps.mayo.configuration.ApplicationConfiguration;
import com.hps.mayo.services.HttpService;

import edu.mayo.mdm.party.services.partyservicedata.UserCredentials;
import edu.mayo.mdm.party.services.partyservicemessages.HubPingRequest;
import edu.mayo.model.dao.MdmPersonDao;

import com.hps.mayo.configuration.*;

public class HttpRequestBuilder {

	protected Logger logger = LogManager.getLogger(this.getClass());

	private final static String METHOD_GET = "GET";
	private final static String METHOD_PUT = "PUT";
	private final static String METHOD_POST = "POST";

	private ApplicationConfiguration appConfig;
	private HttpService httpService;

	public HttpRequestBuilder() {
	}

	public HttpRequestBuilder(ApplicationConfiguration config, HttpService service) {

		this.appConfig = config;
		this.httpService = service;

	}

	public HttpRequestBase buildHttpRequest(String method, String targetUri) {

		HttpRequestBase baseRequest = null;

		if (method == null || method.isEmpty())
			method = "GET"; // Default to GET method

		if (METHOD_POST.equalsIgnoreCase(method))
			baseRequest = new HttpPost(targetUri);
		if (METHOD_PUT.equalsIgnoreCase(method))
			baseRequest = new HttpPut(targetUri);
		if (METHOD_GET.equalsIgnoreCase(method))
			baseRequest = new HttpGet(targetUri);

		baseRequest.setHeader("Content-Type", "application/xml");
		baseRequest.setHeader("Accecpt", "application/xml");
		baseRequest.setHeader("Authorization",
				"Basic " + httpService.getEncodedAuth(appConfig.getHubUserId(), appConfig.getHubUserPassword()));

		return baseRequest;

	}

	public HttpPost getMdmPostRequest() {
		return null;

	}

}
