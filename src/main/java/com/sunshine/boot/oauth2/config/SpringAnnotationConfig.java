package com.sunshine.boot.oauth2.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.sunshine.boot.oauth2.service.ScUserDetailsService;

@Configuration
public class SpringAnnotationConfig {

	@Bean("bcryptPasswordEncoder")
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean("userDetailsService")
	public UserDetailsService userDetailsService() {
		return new ScUserDetailsService();
	}

}
