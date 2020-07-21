package com.sunshine.boot.oauth2.mvc;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartframework.common.utils.ExCacheUtils;
import org.smartframework.common.utils.ExRedisUtils;
import org.smartframework.common.utils.ExStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.sunshine.boot.oauth2.service.yhtjsServerimpl;
import com.sunshine.common.service.SysLoginService;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@RestController
public class RomeTokenController {

	protected final Logger log = LoggerFactory.getLogger(RomeTokenController.class);
	
	@Autowired
	private SysLoginService loginService;
	
	@Autowired
	private yhtjsServerimpl yh;
	
	@RequestMapping(name="退出登录",value = "oauth/logout", produces="application/json", method={RequestMethod.POST, RequestMethod.GET})
	public void removeUserToken(HttpServletRequest request, HttpServletResponse response) throws IOException{
		String return_uri = request.getParameter("post_logout_redirect_uri");
		String yhm=request.getParameter("YgID");		 
		Map resultMap = new HashMap();
		try{
			String loginId = loginService.getCookieNameForLoginId();
			String YgNetLoginToken = loginService.getCookieNameForLoginToken();
			String cacheKey =  "AccessToken-" + loginId;
			String token = (String)ExCacheUtils.getData("UserTokenCache", cacheKey);
			System.out.println("cacheKey: "+cacheKey+"token: "+token);
			log.debug("cacheKey: "+cacheKey+"token: "+token);
			Map useData = (Map)ExCacheUtils.getData("UserTokenCache", token);
			ExCacheUtils.removeCache("UserTokenCache",loginId);
			ExCacheUtils.removeCache("UserTokenCache",token);
			Cookie [] cookie = request.getCookies();				
			if(cookie.length > 0 && cookie != null){
				for (int i = 0; i < cookie.length; i++) {
					if(cookie[i].getName().equalsIgnoreCase(loginId) || cookie[i].getName().equalsIgnoreCase(YgNetLoginToken)){						
						System.out.println("CookieName: "+cookie[i].getValue());
						log.debug("CookieName: "+cookie[i].getValue());
					}
					cookie[i].setMaxAge(0);
				}
			}
			HttpSession session = request.getSession();
			session.invalidate();	
			resultMap.put("resultCode", 1);
			resultMap.put("resultMsg", "退出成功");
			ExRedisUtils.del("User:"+change(new Date())+":"+yhm);
			if (!"".equals(yhm) && yhm != null) {
				try {
					Map user = yh.yhdlqcx(yhm);
					String ip=getRemortIP(request);
					user.put("ip", ip);
					yh.tc(user);
				} catch (Exception e) {
					log.error("用户退出日志记录失败" + e);
				}
			} else {
				try {
					yh.tc(null);
				} catch (Exception e) {
					log.error("用户退出日志记录失败" + e);
				}
			}
			log.debug("DELUser" + yhm);
		} catch (Exception ex) {
			resultMap = ExStringUtils.buildResultMessage(-1,
					"退出出错," + ex.getMessage());
			log.error(ex.getMessage(), ex);
		}
		response.sendRedirect(return_uri);
	}
	
	public static String change(Date d){
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(d);
		
	}
	public String getRemortIP(HttpServletRequest request) {
		String ip = request.getHeader("X-Real-IP");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
			ip = request.getHeader("X-Forwarded-For");
			}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
			} 
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
			ip = request.getHeader("WL-Proxy-Client-IP"); 
			} 
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr(); 
		    }
		return ip;
	}
	
}
