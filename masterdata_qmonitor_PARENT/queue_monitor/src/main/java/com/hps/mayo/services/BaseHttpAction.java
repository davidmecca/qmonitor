package com.hps.mayo.services;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;

import com.hps.mayo.integration.configuration.HttpConstants;

public class BaseHttpAction {

	private final static String METHOD_GET = "GET";
	private final static String METHOD_PUT = "PUT";
	private final static String METHOD_POST = "POST";

	protected String authHashCookie = null;

	public URLConnection handleBasicAuthentication(URL url, String username, String password) throws IOException {
		URLConnection urlConnection = url.openConnection();
		String authString = username + ":" + password;
		String authStringEnc = new String(Base64.encodeBase64(authString.getBytes()));
		urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);
		return urlConnection;
	}

	public void handleBasicAuthentication(URL url, String username, String password, HttpURLConnection httpConn)
			throws Exception {
		String authString = username + ":" + password;
		String authStringEnc = new String(Base64.encodeBase64(authString.getBytes()));

		try {
			httpConn.addRequestProperty(HttpConstants.AUTHORIZATION_HEADER, "Basic " + authStringEnc);
		} catch (Exception e) {
			throw new Exception("Unable to set HTTP Basic Authorization on connection. " + e.getMessage());
		}

	}

	public String getEncodedAuth(String username, String password) {
		String authString = username + ":" + password;
		return new String(Base64.encodeBase64(authString.getBytes()));
	}

	public HttpRequestBase buildHttpRequest(String method, String authHashCookie, String targetUri) {

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
		baseRequest.setHeader("Cookie", authHashCookie);

		return baseRequest;

	}

}
