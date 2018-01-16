package com.hps.mayo.services;

import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.hps.mayo.configuration.AppConstants;
import com.hps.mayo.configuration.ApplicationConfiguration;

@Service
public class HttpService extends BaseHttpAction {

	protected Logger logger = LogManager.getLogger(this.getClass());
	private CloseableHttpClient httpClient = null;
	private RestTemplate restTemplate = null;

	@Autowired
	private ApplicationConfiguration appConfig;

	public HttpService() {
	}

	@PostConstruct
	private void initialize() {

		initializeHttpClient();
		initializeRestTemplate();

	}

	@PreDestroy
	private void destroy() {

		HttpClientUtils.closeQuietly(httpClient);

	}

	public String getAuthHashCookie() {
		return authHashCookie;
	}

	private void setAuthHashCookie(String authHashCookie) {
		this.authHashCookie = authHashCookie;
	}

	private void initializeHttpClient() {

		httpClient = HttpClients.createDefault();

	}

	private void initializeHttpClient(String userId, String pw) {

		httpClient = HttpClients.createDefault();
		if (authHashCookie == null)
			startAuthorizedHTTPClient(userId, pw, appConfig.getActiveEndPoint() + AppConstants.AUTHORIZATION_URI);

	}

	private void initializeRestTemplate() {
		restTemplate = new RestTemplate();
		List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
		messageConverters.add(new MappingJackson2HttpMessageConverter());
		messageConverters.add(new FormHttpMessageConverter());
		messageConverters.add(new Jaxb2RootElementHttpMessageConverter());
		messageConverters.add(new AllEncompassingFormHttpMessageConverter());

		restTemplate.setMessageConverters(messageConverters);
	}

	public RestTemplate getRestTemplate() {
		return restTemplate;
	}

	public CloseableHttpClient getHttpClient() {
		if (httpClient == null)
			initializeHttpClient();

		return httpClient;
	}

	public void startAuthorizedHTTPClient(String user, String pw, String targetUrl) {

		try {
			String httpAuthRequest = "{ user:'" + user + "', password:'" + pw + "'}";
			URL url = new URL(targetUrl);
			URLConnection urlConnection = handleBasicAuthentication(url, user, pw);
			urlConnection.setDoInput(true);
			urlConnection.setDoOutput(true);
			urlConnection.setUseCaches(false);
			urlConnection.setRequestProperty("Content-Type", "application/json");
			OutputStream os = urlConnection.getOutputStream();
			os.write(httpAuthRequest.getBytes());
			os.flush();
			os.close();
			Map<String, List<String>> headerFields = urlConnection.getHeaderFields();

			setAuthCookie(headerFields);

			if (authHashCookie == null)
				logger.fatal("Unable to create a client connection to the target ORS.  Application will not function.");

		} catch (Exception e) {
			logger.error("Error in startAuthorizedHTTPClient call: " + targetUrl + " Error:" + e.getMessage());
			e.printStackTrace();
		}

	}

	private void setAuthCookie(Map<String, List<String>> headerFields) {

		boolean cookieFound = false;
		Set<String> headerFieldSet = headerFields.keySet();
		Iterator<String> headerFieldIter = headerFieldSet.iterator();
		while (headerFieldIter.hasNext() && !cookieFound) {

			String headerFieldKey = headerFieldIter.next();

			if ("Set-Cookie".equalsIgnoreCase(headerFieldKey)) {

				List<String> headerFieldValue = headerFields.get(headerFieldKey);
				for (String headerValue : headerFieldValue) {
					if (cookieFound)
						break;
					String[] fields = headerValue.split(";");
					for (int i = 0; i < fields.length; i++) {
						if (fields[i].toLowerCase().startsWith("auth_hash_cookie")) {
							setAuthHashCookie(fields[i]);
							cookieFound = true;
							break;
						}
					}
				}
			}
		}
	}

	public HttpRequestBase buildHttpPostRequest(String requestUri) {

		HttpRequestBase baseGetRequest = new HttpGet();

		baseGetRequest.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		baseGetRequest.setHeader("Accept-Language", "en-US,en;q=0.5");
		baseGetRequest.setHeader("Connection", "keep-alive");
		baseGetRequest.setHeader("Content-Type", "application/x-www-form-urlencoded");

		try {
			baseGetRequest.setURI(new URI(requestUri));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		return baseGetRequest;

	}

	public URL getPostUri() {

		URL url = null;
		try {
			url = new URL(appConfig.getActiveEndPoint() + AppConstants.PATH_DELIMITER + appConfig.getContextRoot()
					+ AppConstants.PATH_DELIMITER + AppConstants.PUT_URI);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		return url;

	}

	public URL getMergeUri() {

		URL url = null;
		try {
			url = new URL(appConfig.getActiveEndPoint() + AppConstants.PATH_DELIMITER + appConfig.getContextRoot()
					+ AppConstants.PATH_DELIMITER + AppConstants.MERGE_URI);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		return url;

	}

	public URL getUnmergeUri() {

		URL url = null;
		try {
			url = new URL(appConfig.getActiveEndPoint() + AppConstants.PATH_DELIMITER + appConfig.getContextRoot()
					+ AppConstants.PATH_DELIMITER + AppConstants.UMERGE_URI);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		return url;

	}

	public URL getPingUri() {

		URL url = null;
		try {
			url = new URL(appConfig.getActiveEndPoint() + AppConstants.PATH_DELIMITER + appConfig.getContextRoot()
					+ AppConstants.PATH_DELIMITER + AppConstants.PING_URI);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		return url;

	}

}
