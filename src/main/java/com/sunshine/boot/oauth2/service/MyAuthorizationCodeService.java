package com.sunshine.boot.oauth2.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.common.util.RandomValueStringGenerator;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.code.RandomValueAuthorizationCodeServices;

import com.alibaba.fastjson.JSON;

public class MyAuthorizationCodeService extends RandomValueAuthorizationCodeServices {

	protected static final Logger log = LoggerFactory.getLogger(MyAuthorizationCodeService.class);

	protected final ConcurrentHashMap<String, OAuth2Authentication> authorizationCodeStore = new ConcurrentHashMap<String, OAuth2Authentication>();

	private RandomValueStringGenerator generator = new RandomValueStringGenerator(16);

	private ClientDetailsService clientDetailsService;
	
	public void setClientDetailsService(ClientDetailsService clientDetailsService) {
		this.clientDetailsService = clientDetailsService;
	}
	
	@Override
	protected void store(String code, OAuth2Authentication authentication) {
		this.authorizationCodeStore.put(code, authentication);
	}

	@Override
	public OAuth2Authentication remove(String code) {
		OAuth2Authentication auth = this.authorizationCodeStore.remove(code);
		return auth;
	}

	@Override
	public String createAuthorizationCode(OAuth2Authentication authentication) {
		String clientId = authentication.getOAuth2Request().getClientId();
		ClientDetails clientDetails = this.clientDetailsService.loadClientByClientId(clientId);
		Map userInfo = new HashMap();
		userInfo.put("loginId", authentication.getUserAuthentication().getName());
		userInfo.put("name", authentication.getUserAuthentication().getName());
	
		String code = generator.generate();
		String data = code + "," + new String(Base64.encodeBase64(clientId.getBytes()));
		String signature = hmacSHA256(data, clientDetails.getClientSecret());
		String payload = new String(Base64.encodeBase64(JSON.toJSONString(userInfo).getBytes()));
		code = code + "." + payload + "." + signature;
		store(code, authentication);
		return code;
	}

	
	protected String hmacSHA256(String data, String password) {
			
		try{
			
			// 初始化HmacMD5摘要算法的密钥产生器  
	        KeyGenerator generator = KeyGenerator.getInstance("HmacSHA256");  
	        // 产生密钥  
	        SecretKey secretKey = generator.generateKey();  
			// 还原密钥  
	        SecretKey secretKeySpec = new SecretKeySpec(password.getBytes(), "HmacSHA256");  
	        // 实例化Mac  
	        Mac mac = Mac.getInstance(secretKey.getAlgorithm());  
	      
	        //初始化mac  
	        mac.init(secretKeySpec);  
	        
	        //执行消息摘要  
	        byte[] digest = mac.doFinal(data.getBytes());
	        
	        //转为BASE64的字符串 
	        String signature = convertData(Base64.encodeBase64(digest));
	        return signature;
	        
		}catch(Exception ex){
			log.error(ex.getMessage(), ex);
		}		
		return null;
	}
	
	private String convertData(byte[] input){
		String temp = new String(input);
		temp = temp.split("=")[0]; // Remove any trailing '='s
		temp = temp.replace('+', '-'); // 62nd char of encoding
		temp = temp.replace('/', '_'); // 63rd char of encoding
        return temp;	
	}
	
}
