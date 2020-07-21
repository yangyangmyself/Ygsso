package com.sunshine.boot.oauth2.config;

import org.smartframework.common.GlobalConfig;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.builders.ClientDetailsServiceBuilder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import com.sunshine.boot.oauth2.service.MyAuthorizationCodeService;

/**
 * AS配置
 * @author liumeng 2018-06-01
 *
 */
@Configuration
@EnableAuthorizationServer
public class OAuth2ServerConfig extends AuthorizationServerConfigurerAdapter  implements InitializingBean {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private Environment environment;


    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
    	
    	MyAuthorizationCodeService authorizationCodeServices = authorizationCodeServices();
    	authorizationCodeServices.setClientDetailsService(endpoints.getClientDetailsService());
    	
        endpoints.tokenStore(tokenStore())
                .tokenEnhancer(jwtTokenEnhancer())
                .authorizationCodeServices(authorizationCodeServices)
                .authenticationManager(authenticationManager);
        
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security.tokenKeyAccess("permitAll()")
        			.checkTokenAccess("isAuthenticated()");
    }

    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(jwtTokenEnhancer());
    }

    @Bean
    public MyAuthorizationCodeService authorizationCodeServices(){
    	return new MyAuthorizationCodeService();
    }
    
    @Bean
    protected JwtAccessTokenConverter jwtTokenEnhancer() {
//        String pwd = environment.getProperty("keystore.password");
    	String pwd=GlobalConfig.getConfigValue("keystore.password");
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(
                new ClassPathResource("jwt.jks"),
                pwd.toCharArray());
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setKeyPair(keyStoreKeyFactory.getKeyPair("jwt"));
        return converter;
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
    	ClientDetailsServiceBuilder builder =  clients.inMemory();
    	
    	builder
                .withClient("service-account-1")
                .secret("service-account-1-secret")
                .authorizedGrantTypes("client_credentials","authorization_code","password")
                .scopes("resource-server-read", "resource-server-write")
                .accessTokenValiditySeconds(8*60*60).autoApprove(true);
    	
    	builder
	        .withClient("SheChe")
	        .secret("12345678")
	        .authorizedGrantTypes("client_credentials","authorization_code","password")
	        .scopes("resource-server-read", "resource-server-write")
	        .accessTokenValiditySeconds(8*60*60).autoApprove(true);
    	
    	builder.build();

    }

	@Override
	public void afterPropertiesSet() throws Exception {
		GlobalConfig.environment=environment;
	}

}