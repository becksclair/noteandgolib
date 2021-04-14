package com.heliasar.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class TrackStopApi {

	// API Base URL
	static final String API_BASE_URL = /* Config.DEBUG ? "http://10.0.2.2/trackstop/" :*/ "http://heliasar.com/trackstop/";
	static final String API_TRACKING = API_BASE_URL;

	public static class Response {
		public int statusCode;
		public String resp;
	}

	public static Response post(String url, String postData) {
		Response response = new Response();
		try {
			disableSSLCertificateChecking();
			
			Utils.l("Connecting to " + url);

			HttpURLConnection conn = (HttpURLConnection) (new URL(url)).openConnection();
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
			
			Utils.l("Connecting to " + url);

			// Setup connection
			HttpURLConnection conn = (HttpURLConnection) (new URL(url)).openConnection();
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
			Utils.l(e.getMessage());
			response.statusCode = 401;
		}
		return response;
	}

	public static Response delete(String url) {
		Response response = new Response();
		try {
			// Setup connection
			HttpURLConnection conn = (HttpURLConnection) (new URL(url)).openConnection();
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

	public static String encode(String str, Boolean base64Encode) {
		try {
			return base64Encode ? Base64.encodeBytes(URLEncoder.encode(str,
					"UTF-8").getBytes()) : URLEncoder.encode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}
	
	public static String decode(String str) {
		try {
			return new String(Base64.decode(str), "UTF-8");
		} catch (IOException e) {
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
