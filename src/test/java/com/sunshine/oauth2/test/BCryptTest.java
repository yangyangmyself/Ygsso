package com.sunshine.oauth2.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCrypt;

public class BCryptTest {

	private static final Logger logger = LoggerFactory.getLogger(BCryptTest.class);

	public static void main(String[] args) {
		//获取BCrypt加密后的密码
		String password = "180619";
		String bcryptPd = BCrypt.hashpw(password, BCrypt.gensalt());
		System.out.println(bcryptPd);
		
		boolean result = BCrypt.checkpw(password, bcryptPd);
		System.out.println(result);
	}
}
