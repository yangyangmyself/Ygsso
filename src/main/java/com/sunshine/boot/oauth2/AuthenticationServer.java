package com.sunshine.boot.oauth2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@Configuration
@ImportResource("classpath:/spring/applicationContext.xml")
@EnableAutoConfiguration
@ComponentScan
@RefreshScope
@RestController
public class AuthenticationServer  {
	private static final Logger logger = LoggerFactory.getLogger(AuthenticationServer.class);

	
	public static void main(String[] args) {
    	
    	logger.info("OAUTH2 服务开始启动了");
    	// 
    	SpringApplication.run(AuthenticationServer.class, args);
    }



}
