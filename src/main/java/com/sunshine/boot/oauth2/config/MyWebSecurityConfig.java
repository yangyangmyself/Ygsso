package com.sunshine.boot.oauth2.config;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AbstractAuthenticationTargetUrlRequestHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import com.sunshine.boot.oauth2.authentication.PKIAuthenticationFilter;
//import com.sunshine.boot.oauth2.authentication.SSLCodeAuthenticationSecurityConfig;
import com.sunshine.boot.oauth2.service.yhtjsServerimpl;

/**
 * 客户端的安全配置
 *
 * @author liumeng 2018-06-01
 */
@Configuration
@EnableWebSecurity
@Component
public class MyWebSecurityConfig extends WebSecurityConfigurerAdapter {

    protected final Logger log = LoggerFactory.getLogger(MyWebSecurityConfig.class);

    LoginSuccessHandler loginSuccessHandler = new LoginSuccessHandler();
    LogoutSuccessHandler logoutSuccessHandler = new MyLogoutSuccessHandler();
    MyFailureHandler failureHandler = new MyFailureHandler();
    
   // @Autowired
    //SSLCodeAuthenticationSecurityConfig sslCodeAuthenticationSecurityConfig;
    
    @Autowired
    @Qualifier("userDetailsService")
    private UserDetailsService userDetailsService;
    @Autowired
	private yhtjsServerimpl yhm;
   
    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    @Autowired
    @Qualifier("bcryptPasswordEncoder")
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    @Qualifier("pkiAuthenticationFilter")
    private PKIAuthenticationFilter pkiAuthenticationFilter;

    @Autowired
	@Qualifier("sslAuthenticationProvider")
	private AuthenticationProvider sslAuthenticationProvider;
    

    @Override 
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/", "/index.html", "/images/**", "/login/**", "/assets/**", "/theme/**", "/oauth/uncache_approvals", "/oauth/cache_approvals");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //String redirect_uri = GlobalConfig.getConfigValue("redirect.uri", "http://192.168.2.254");
        //log.debug(redirect_uri);
    	
    	http.addFilterBefore(pkiAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    	
        http
                .authorizeRequests()
                .antMatchers("/getVerify","/checkImage","/oauth2/token", "/oauth2/tokeninfo", "/api/login/**", "/sso/login/**", "/api/data/**", "/api/demo/**").permitAll()
                .anyRequest().authenticated()

                .and()
                
                .formLogin()
                .loginPage("/login/pkilogin.html")//登录
                // 普通登录配置
                .loginProcessingUrl("/sso/login")//后端处理
                .successForwardUrl("/oauth/authorize?response_type=code&client_id=SheChe&redirect_uri=http://localhost:8066/2/")//登录成功后跳转的url
                .failureHandler(failureHandler)
                
                //.and()
                //.apply(sslCodeAuthenticationSecurityConfig) // 注册PKI配置
                
                .and()
                
                .csrf()
                
                .disable()
                
                .logout()
                .logoutSuccessHandler(logoutSuccessHandler)
                //.logoutSuccessUrl("/login/pkilogin.html")
                .permitAll();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder);

    	/*auth.inMemoryAuthentication()
                .withUser("admin").password("123456").roles("USER","ADMIN");*/

        // 注入自定义AuthenticationProvider（无法获取到默认UserDetailsService，即不使用默认UserDetailsService）
     	// 自定AuthenticationProvider对象注入自定义UserDetailsService
     	auth.authenticationProvider(sslAuthenticationProvider);
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    class LoginSuccessHandler implements AuthenticationSuccessHandler {
        private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
        @Override
        public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException,
                ServletException {
            //Principal user = (Principal) authentication.getPrincipal();
            System.out.println("登录成功");
            redirectStrategy.sendRedirect(request, response, "/home.html");
        }
    }

    class MyLogoutSuccessHandler extends AbstractAuthenticationTargetUrlRequestHandler implements LogoutSuccessHandler {

        private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

        @Override
        public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
            String returnUrl = request.getParameter("post_logout_redirect_uri");
            System.out.println("登出成功");
            if (returnUrl != null) {
                //清除redis登录信息
                redirectStrategy.sendRedirect(request, response, returnUrl);
            } else {
                super.handle(request, response, authentication);
            }

        }
    }

    public class MyFailureHandler implements AuthenticationFailureHandler {

        @Override
        public void onAuthenticationFailure(HttpServletRequest request,
                                            HttpServletResponse response, AuthenticationException exception)
                throws IOException, ServletException {
            String return_uri = request.getParameter("post_logout_redirect_uri");
            String username=request.getParameter("username");
        	try{
				Map user=new HashMap<>(); 
				user=	yhm.yhdlqcx(username);    				
				yhm.dl(user,"您输入的用户名或密码有误，请重新输入！");
				}catch (Exception e) {
			log.error("用户登录失败日志记录出错============================"+e.getMessage());	
				}
            if (StringUtils.isBlank(return_uri)) {
//                return_uri = getLocalServerUrl(request) + "/login/index.html";
            	 return_uri = getLocalServerUrl(request) + "/login/pkilogin.html";
            }
            String error = "您输入的用户名或密码有误，请重新输入！";                		    		
            error = URLEncoder.encode(error, "UTF-8");
            response.sendRedirect(return_uri + "?error=" + error);
        }

        protected String getLocalServerUrl(HttpServletRequest request) {
            String contextPath = request.getContextPath();
            String requestURI = request.getRequestURI();
            String localServerUrl = request.getRequestURL().toString();
            int indexPos = localServerUrl.indexOf(requestURI);
            if (indexPos > 0) {
                localServerUrl = localServerUrl.substring(0, (indexPos + contextPath.length()));
            }
            return localServerUrl;
        }

    }

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println(encoder.encode("888888"));
//         $10$q.mSGEjFl6D.erhyTAkSQ.MMw1oenV2rH.sErt.1uWsq0UEt8bIae

        System.out.println(encoder.matches("888888", "$2a$10$p80b0g/OQwhlow/TJTHovOXu9HzJEM5Q6B.GKRilYYDcsWyUS9LjK"));
    }


}
