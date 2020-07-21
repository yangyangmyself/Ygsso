package com.sunshine.boot.oauth2.mvc;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.ValidateUtils;

@RestController
public class UserController {

	private static final Logger logger = LoggerFactory.getLogger(UserController.class);

	
    @RequestMapping("/user")
    public Principal user(HttpServletRequest request, Principal user) {
    	
    	logger.info("debug: {}" ,request.getUserPrincipal());
    	
        //logger.info("AS /user has been called");
        //logger.debug("user info: " + user.toString());
        return user;
    }
    @RequestMapping("/checkImage")
    public String getLogin(HttpServletRequest request) {
    	return (String) request.getSession().getAttribute("RANDOMVALIDATECODEKEY");
    }
    
    @RequestMapping(value = "/getVerify",produces="application/json")
    public void getVerify(HttpServletRequest request, HttpServletResponse response) {
        try {
            response.setContentType("image/jpeg");//设置相应类型,告诉浏览器输出的内容为图片
            response.setHeader("Pragma", "No-cache");//设置响应头信息，告诉浏览器不要缓存此内容
            response.setHeader("Cache-Control", "no-cache");
            response.setDateHeader("Expire", 0);
            //ValidateUtils randomValidateCode = new ValidateUtils();
            ValidateUtils.getRandcode(request, response);//输出验证码图片方法
        } catch (Exception e) {
        	logger.error("获取验证码失败>>>>   ", e);
        }
    }
   
    
}
