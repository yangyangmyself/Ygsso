package com.sunshine.boot.oauth2.config;

import org.apache.catalina.connector.Connector;
import org.smartframework.common.GlobalConfig;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class TomcatHttpConfig  implements InitializingBean{
	@Autowired
	private Environment environment;
    /**
     * 配置内置的servlet容器工厂为tomcat.
     * @return
     */
    @Bean
    public EmbeddedServletContainerFactory servletContainer() {
        TomcatEmbeddedServletContainerFactory tomcat = new TomcatEmbeddedServletContainerFactory();
        tomcat.addAdditionalTomcatConnectors(initiateHttpConnector()); // 添加http
        return tomcat;
    }

    /**
     * 配置一个http连接信息.
     * @return
     */
    private String prod="8077";
    private Connector initiateHttpConnector() {
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        	
//            connector.setPort();
//            String port = GlobalConfig.getConfigValue("http.port","8066");
//        	connector.setPort(18443);
            connector.setPort(Integer.parseInt(this.prod));

            return connector;


    }

	@Override
	public void afterPropertiesSet() throws Exception {
		GlobalConfig.environment=environment;
		this.prod= GlobalConfig.getConfigValue("sso.serverPort","8066");
		
	}
}

