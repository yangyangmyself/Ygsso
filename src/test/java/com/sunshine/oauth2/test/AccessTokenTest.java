package com.sunshine.oauth2.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;



public class AccessTokenTest {

	private static final Logger logger = LoggerFactory.getLogger(AccessTokenTest.class);


	private String serverUrl = "http://localhost:8080/ssov6";
	private String client_id = "service-account-1";
	private String client_secret = "service-account-1-secret";

//	public String oauthToken(){
//
//		String token = null;
//		int expires_in = 0;
//		String requestUrl = serverUrl + "/oauth/token";
//		Map paramMap = new HashMap();
//		paramMap.put("grant_type", "client_credentials");
//
//
//		Map headerMap = new HashMap();
//		headerMap.put("Authorization", "Basic " + ExDigestUtils.base64Encode(client_id + ":" + client_secret));
//		String resultText = ExHttpClientUtils.doPost(requestUrl, paramMap, headerMap);
//
//		logger.info("oauthToken：" + resultText);
//		if (resultText != null){
//			JSONObject json = JSON.parseObject(resultText);
//			token = json.getString("access_token");
//			expires_in = json.getIntValue("expires_in");
//		}
//		return token;
//	}
//
//
//	public void checkToken(String token){
//
//		String requestUrl = serverUrl + "/oauth/check_token";
//		Map paramMap = new HashMap();
//		paramMap.put("token", token);
//
//		Map headerMap = new HashMap();
//		headerMap.put("Authorization", "Basic " + ExDigestUtils.base64Encode(client_id + ":" + client_secret));
//
//		String resultText = ExHttpClientUtils.doPost(requestUrl, paramMap, headerMap);
//		logger.info("checkToken：" + resultText);
//
//	}
//
//
//	public static void main(String[] args) {
//
//		AccessTokenTest test = new AccessTokenTest();
//		//
////		String token = test.oauthToken();
////		test.checkToken(token);
//
//
//	}

	public static final String httpURL = "http://127.0.0.1:8066/ssov6/";

	@Test
	public void testGetCodeByAuthorizationCode(){
		HttpClient client = HttpClients.createDefault();
		try {
			HttpPost post = new HttpPost(httpURL + "oauth/authorize");
			List<NameValuePair> params=new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("client_id","SheChe"));
			params.add(new BasicNameValuePair("scope","resource-server-read"));
			params.add(new BasicNameValuePair("response_type","code"));
//			params.add(new BasicNameValuePair("grant_type","authorization_code"));
			params.add(new BasicNameValuePair("state","xyz"));
//			params.add(new BasicNameValuePair("redirect_uri","http://example.com"));
			post.setEntity(new UrlEncodedFormEntity(params));
			post.addHeader("Authorization", "Basic " + base64EncodeStr("yangguangnaite" + ":" + "admin"));
			HttpResponse response = client.execute(post);
			int scode = response.getStatusLine().getStatusCode();
			System.out.println(scode + "--" + EntityUtils.toString(response.getEntity()));
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testGetTokenByAuthorizationCode(){
		HttpClient client = HttpClients.createDefault();
		try {
			HttpPost post = new HttpPost(httpURL + "oauth/token");
			List<NameValuePair> params=new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("response_type","token"));
			params.add(new BasicNameValuePair("grant_type","authorization_code"));
			// 上次请求生成的CODE
			params.add(new BasicNameValuePair("code","302"));
			params.add(new BasicNameValuePair("redirect_uri","http://example.com"));;
			post.setEntity(new UrlEncodedFormEntity(params));
			// 客户端ID及密码
			post.addHeader("Authorization", "Basic " + base64EncodeStr("Sch:" + "secret"));
			HttpResponse response = client.execute(post);
			int scode = response.getStatusLine().getStatusCode();
			System.out.println(scode + "--" + EntityUtils.toString(response.getEntity()));
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String base64EncodeStr(String str) {

		return Base64.encodeBase64String(str.getBytes());
	}
}
