package com.sunshine.boot.oauth2.unifiedUser;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.smartframework.common.DataObject;
import org.smartframework.common.GlobalConfig;
import org.smartframework.common.utils.ExHttpClientUtils;
import org.smartframework.common.utils.ExRedisUtils;
import org.smartframework.common.utils.ExStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.sunshine.boot.oauth2.dao.UserDaoRepos;
import com.sunshine.common.utils.ExControllerUtils;

@Controller
@RequestMapping({"/user"})
public class UserControllerSSO {
	
	@Autowired
	private UserDaoRepos userDaoRepos;

    /**
     * 获取用户信息
     * @param request
     * @return   , method = {RequestMethod.GET}
     */
    @RequestMapping(value = {"/info"})
    @ResponseBody
    public String info(HttpServletRequest request, HttpServletResponse response) {
        try {
            String userId = request.getSession().getAttribute("securityUser")!=null?request.getSession().getAttribute("securityUser").toString():null;
            Map<String,Object> params = new HashMap<>();
            params.put("title", "用户登录");
            params.put("typename", "登录");
        	params.put("cznr", "操作结果:登录成功");
            insetLog(request,params,userId);
            return userId;
        } catch (Exception var3) {
            throw new SecurityException("获取用户信息异常", var3);
        }
    }
    
    public void insetLog(HttpServletRequest request,Map<String,Object> param,String userId) {
    	Map<String,Object> userInfo = new HashMap<String, Object>();
        if(userId != null && !"".equals(userId)){
    		userInfo = ExRedisUtils.getMap(userId);
    		System.out.println("UserControler====ExRedisUtils========="+userInfo);
    	}
    	
    	if(userInfo.get("sfzmhm") == null || "".equals(userInfo.get("sfzmhm")) || userInfo.get("jh") == null || "".equals(userInfo.get("jh"))){
    		userInfo.put("resultCode", "0");
    		userInfo.put("resultMsg", "身份证号码或警号不能为空");
//			return userInfo;
		}else {
			String sql = " select u.sex,u.lxdh1 as ulxdh1,u.yhdh,u.yhmc,d.glbm,d.bmmc from t_sys_sysuser u , t_sys_department d where 1=1 ";
    		/**/if(userInfo.get("sfzmhm") != null && !"".equals(userInfo.get("sfzmhm"))){
    			sql = sql + " and u.sfzmhm = '" + String.valueOf(userInfo.get("sfzmhm")) + "' ";
    		}
    		if(userInfo.get("jh") != null && !"".equals(userInfo.get("jh"))){
    			sql = sql + " and u.jh = '" + String.valueOf(userInfo.get("jh")) + "' ";
    		}
    		sql = sql + " and u.glbm = d.glbm LIMIT 1";
    		Map rsMap = new HashMap<>();
    		rsMap = this.userDaoRepos.findOneBySQL(sql, null);
    		if(rsMap.get("sex") == null || "".equals(rsMap.get("sex"))){
    			rsMap.put("sex",".");
    		}
    		userInfo.putAll(rsMap);
		}
		
        Map<String,Object> params = new HashMap<>();
        String ip = request.getHeader("x-forwarded-for");
    	if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
    		ip = request.getHeader("Proxy-Client-IP");
    	}
    	if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
    		ip = request.getHeader("WL-Proxy-Client-IP");
    	}
    	if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
    		ip = request.getHeader("HTTP_CLIENT_IP");
    	}
    	if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
    		ip = request.getHeader("HTTP_X_FORWARDED_FOR");
    	}
    	if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
    		ip = request.getRemoteAddr();
    	}
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
    	ip = ip.equals("0:0:0:0:0:0:0:1") ? "127.0.0.1" : ip;
    	params.put("ip", ip);
    	params.put("title", param.get("title"));
    	params.put("sourcetype", "0");
    	params.put("sourcetype", "电脑端");
    	params.put("type", "6");
    	params.put("typename", param.get("typename"));
    	params.put("cznr", param.get("cznr"));
    	params.put("jh", userInfo.get("jh"));
    	params.put("sfzmhm", userInfo.get("sfzmhm"));
    	params.put("cjr", userInfo.get("yhdh"));
    	params.put("cjrmc", userInfo.get("yhmc"));
    	params.put("city", userInfo.get("glbm").toString().substring(0, 6));
    	params.put("cjdw", userInfo.get("glbm"));
    	params.put("cjdwmc", userInfo.get("bmmc"));
    	params.put("cjsj", sdf.format(date));
    	params.put("status", "0");

        userDaoRepos.insertLong(params);
    }
    
    @RequestMapping(value = {"/get/info"})
    @ResponseBody
    public Map<String,Object> getUserInfo(HttpServletRequest request){
    	Map<String,Object> userInfo = new HashMap<String, Object>();
    	Map<String,Object> params = new HashMap<String, Object>();
    	try {
			// 获取参数
//			params = ExControllerUtils.buildParametersMap(request);
			request.setCharacterEncoding("UTF-8");
			String contentType = request.getContentType();
			System.out.println("contentType========="+contentType);
			String method = request.getMethod();
			System.out.println("method========="+method);
			DataObject paramMap = new DataObject();
		
			if ("application/json".equalsIgnoreCase(contentType) && ("Post".equalsIgnoreCase(method) || "Put".equalsIgnoreCase(method))) {
				// 处理JSON数据流方式提交的参数
				String queryParams = null;
				try{
					queryParams = IOUtils.toString(request.getInputStream(), "UTF-8");
					System.out.println("queryParams======"+queryParams);
				}catch(IOException ex){
//					log.error("request.getInputStream() has error...... ");
					ex.printStackTrace();
					System.out.println(ex.getMessage());
				}
				if (queryParams == null || queryParams.length() == 0) {
					JSONObject jsonParams = new JSONObject();
					Enumeration<String> names = request.getParameterNames();
					while (names.hasMoreElements()) {
						String paramName = names.nextElement();
						System.out.println("paramName=========="+paramName);
						if (!"v,time,nonce,token".contains(paramName)) {
							if (request.getParameter(paramName) != null) {
								jsonParams.put(paramName, request.getParameter(paramName));
							} else {
								jsonParams.put(paramName, ExStringUtils.concat(request.getParameterValues(paramName)));
							}
						}
					}
					queryParams = jsonParams.toJSONString();
				}

				Map tmpMap = ExStringUtils.parseJsonParameters(queryParams);
				System.out.println("tmpMap======="+tmpMap);
				if (tmpMap == null) {
					tmpMap = ExStringUtils.parseUrlParameters(queryParams);
				}
				params.putAll(tmpMap);

			} else {
				// 处理以FORM方式提交参数
				Enumeration names = request.getParameterNames();
				while (names.hasMoreElements()) {
					String name = (String) names.nextElement();
					String value = request.getParameter(name);
					params.put(name, value);
				}
			}

			// 获取当前登录用户ID
//			paramMap.put("loginUserId", getLoginId(request));

//			return paramMap;
    	} catch (Exception e) {
    		System.out.println("查询用户信息异常"+e.getMessage());
    		e.printStackTrace();
			userInfo.put("resultCode", 1);
			userInfo.put("resultMsg", "查询用户信息异常");
		}
    	String userinfotoken = params.get("userinfo_token") == null ? "" : params.get("userinfo_token").toString(); // 标签编号
    	System.out.println("userinfotoken=========" + userinfotoken);
    	if(userinfotoken != null && !"".equals(userinfotoken)){
    		userInfo = ExRedisUtils.getMap(userinfotoken);
    		System.out.println("UserControler====ExRedisUtils========="+userInfo);
    	}
    	
    	if(userInfo.get("sfzmhm") == null || "".equals(userInfo.get("sfzmhm")) || userInfo.get("jh") == null || "".equals(userInfo.get("jh"))){
    		userInfo.put("resultCode", "0");
    		userInfo.put("resultMsg", "身份证号码或警号不能为空");
			return userInfo;
		}
		String sql = " select u.sex,u.lxdh1 as ulxdh1,u.yhdh,u.yhmc,d.glbm,d.bmmc from t_sys_sysuser u , t_sys_department d where 1=1 ";
		/**/if(userInfo.get("sfzmhm") != null && !"".equals(userInfo.get("sfzmhm"))){
			sql = sql + " and u.sfzmhm = '" + String.valueOf(userInfo.get("sfzmhm")) + "' ";
		}
		if(userInfo.get("jh") != null && !"".equals(userInfo.get("jh"))){
			sql = sql + " and u.jh = '" + String.valueOf(userInfo.get("jh")) + "' ";
		}
		sql = sql + " and u.glbm = d.glbm LIMIT 1";
		Map rsMap = new HashMap<>();
		rsMap = this.userDaoRepos.findOneBySQL(sql, null);
		if(rsMap.get("sex") == null || "".equals(rsMap.get("sex"))){
			rsMap.put("sex",".");
		}
		userInfo.putAll(rsMap);
    	System.out.println("userInfo========="+userInfo);
    	return userInfo;
    }
    
    //短信发送地址
  	private static String ip = GlobalConfig.getConfigValue("msn.url.addr", "192.168.2.254:8088");
    
    @RequestMapping(value = {"/get/sms"})
    @ResponseBody
    public Map<String,Object> getSMS(HttpServletRequest request){
    	Map<String,Object> result = new HashMap<>();
    	Map<String,Object> param = new HashMap<>();
    	Map<String,Object> params = new HashMap<>();
    	try {
			// 获取参数
			params = ExControllerUtils.buildParametersMap(request);
    	} catch (Exception e) {
    		System.out.println("查询用户信息异常"+e.getMessage());
    		e.printStackTrace();
    		result.put("resultCode", 1);
    		result.put("resultMsg", "查询用户信息异常");
		}
//    	String userId = request.getSession().getAttribute("securityUser")!=null?request.getSession().getAttribute("securityUser").toString():null;
    	Cookie[] cookies = request.getCookies();
    	String userId = "";
    	for (Cookie cookie : cookies) {
    	    switch(cookie.getName()){
    	        case "tokenServerName":
    	        	userId = cookie.getValue();
    	            break;
    	        default:
    	            break;
    	    }
    	}
    	/*if(userId != null && !"".equals(userId)){
    		userInfo = ExRedisUtils.getMap(userId);
    	}*/
    	String url = "http://" + ip + "/vw/api/service/phone/warning/send";
    	String random = new Random().nextInt(1000000)+"";
    	String tamp = "您的验证码为"+random+"，请于15分钟内正确输入，如非本人操作，请忽略此短信。";
    	param.put("phone", params.get("phone"));
		param.put("msg", tamp);
	    param.put("title", "登录验证码");
	    ExRedisUtils.put(userId, random,900);
	    System.out.println("登录验证码短信参数=========param======="+param);
		String s = ExHttpClientUtils.doPost(url,param);
		System.out.println("============="+s);
		result.put("resultCode", 0);
		result.put("resultMsg", "发送验证码成功");
    	return result;
    }
    
    @RequestMapping(value = {"/get/verification"})
    @ResponseBody
    public Map<String,Object> getVerification(HttpServletRequest request){
//    	String userId = request.getSession().getAttribute("securityUser")!=null?request.getSession().getAttribute("securityUser").toString():null;
    	Cookie[] cookies = request.getCookies();
    	String userId = "";
    	for (Cookie cookie : cookies) {
    	    switch(cookie.getName()){
    	        case "tokenServerName":
    	        	userId = cookie.getValue();
    	            break;
    	        default:
    	            break;
    	    }
    	}
    	Map<String,Object> result = new HashMap<>();
    	Map<String,Object> params = new HashMap<>();
    	try {
			// 获取参数
			params = ExControllerUtils.buildParametersMap(request);
    	} catch (Exception e) {
    		System.out.println("获取参数信息异常"+e.getMessage());
    		e.printStackTrace();
    		result.put("resultCode", 1);
    		result.put("resultMsg", "获取参数信息异常");
		}
    	String verification = ExRedisUtils.get(userId);
    	System.out.println("redis=====verification===="+verification);
    	System.out.println("params=====verification===="+params.get("verification"));
    	if(verification.equals(params.get("verification"))){
    		result.put("resultCode", 0);
    		result.put("resultMsg", "验证成功");
    		return result;
    	}else {
    		result.put("resultCode", 1);
    		result.put("resultMsg", "验证失败");
    		return result;
    	}
    }
}
