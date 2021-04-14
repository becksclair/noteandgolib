package com.heliasar.simplenote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.heliasar.tools.Base64;

public class WebHelper {

	// API Base URL
	static final String API_BASE_URL = "https://simple-note.appspot.com/api";
	static final String API_BASE_URL2 = "https://simple-note.appspot.com/api2";
	public static final String API_LOGIN_URL = API_BASE_URL + "/login"; // POST
	public static final String API_NOTES_URL = API_BASE_URL2 + "/index"; // GET
	public static final String API_CREATE_URL = API_BASE_URL2 + "/data"; // POST
	public static final String API_NOTE_URL = API_BASE_URL2 + "/data/"; // GET
	static final String API_UPDATE_URL = API_BASE_URL2 + "/note"; // POST
	static final String API_DELETE_URL = API_BASE_URL2 + "/delete"; // POST
	static final String API_SEARCH_URL = API_BASE_URL2 + "/search"; // GET

	public static class Response {
		public int statusCode;
		public String resp;
	}

	public static Response post(String url, String postData) {
		Response response = new Response();
		try {
			disableSSLCertificateChecking();

			HttpsURLConnection conn = (HttpsURLConnection) (new URL(url)).openConnection();
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.connect();

			// Send POST data to the server
			OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
			out.write(postData);
			out.flush();

			// Get the response from the server
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder sb = new StringBuilder();

			for (String line = in.readLine(); line != null; line = in.readLine()) {
				sb.append(line + '\n');
			}

			// Store response information in Response object
			try {
				response.statusCode = conn.getResponseCode();

			} catch (IOException e) {
				e.printStackTrace();
				response.statusCode = 401;
			}
			response.resp = sb.toString();

			// Clean up
			conn.disconnect();
			conn = null;
			out = null;
			in = null;
			sb = null;

		} catch (IOException e) {
			response.statusCode = 401;
		}
		return response;
	}

	public static Response get(String url) {
		Response response = new Response();
		try {
			disableSSLCertificateChecking();

			// Setup connection
			HttpsURLConnection conn = (HttpsURLConnection) (new URL(url)).openConnection();
			conn.setRequestMethod("GET");
			conn.connect();

			// Get response from the server
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder sb = new StringBuilder();

			for (String line = in.readLine(); line != null; line = in.readLine()) {
				sb.append(line + '\n');
			}

			// Store response in a Response object
			try {
				response.statusCode = conn.getResponseCode();

			} catch (IOException e) {
				response.statusCode = 401;
			}
			response.resp = sb.toString();

			// Clean up
			conn.disconnect();
			conn = null;
			in = null;
			sb = null;
		} catch (IOException e) {
			response.statusCode = 401;
		}
		return response;
	}

	public static Response delete(String url) {
		Response response = new Response();
		try {
			// Setup connection
			HttpsURLConnection conn = (HttpsURLConnection) (new URL(url)).openConnection();
			conn.setRequestMethod("DELETE");
			conn.setDoOutput(true);
			conn.connect();

			// Get the response from the server
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder sb = new StringBuilder();

			for (String line = in.readLine(); line != null; line = in.readLine()) {
				sb.append(line + '\n');
			}

			// Store response information in Response object
			try {
				response.statusCode = conn.getResponseCode();

			} catch (IOException e) {
				response.statusCode = 401;
			}
			response.resp = sb.toString();

			// Clean up
			conn.disconnect();
			conn = null;
			in = null;
			sb = null;

		} catch (IOException e) {
			response.statusCode = 401;
		}
		return response;
	}

	public static String encode(String str) {
		return encode(str, false); // Don't Base64 encode by default
	}

	/*
	public static String encode(String str, Boolean base64Encode) {
		try {
			return base64Encode ? Base64.encodeBytes(URLEncoder.encode(str,
					"UTF-8").getBytes()) : URLEncoder.encode(str, "UTF-8");
		} catch (Exception e) {
			return null;
		}
	}
	*/
	
	public static String encode(String str, Boolean base64Encode) {
		try {
			return base64Encode ? Base64.encodeBytes(str.getBytes()) : str;
		} catch (Exception e) {
			return null;
		}
	}

	private static void disableSSLCertificateChecking() {
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(X509Certificate[] arg0, String arg1) {
			}

			public void checkServerTrusted(X509Certificate[] arg0, String arg1) {
			}
		} };

		try {
			SSLContext sc = SSLContext.getInstance("TLS");

			sc.init(null, trustAllCerts, new java.security.SecureRandom());

			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

}
