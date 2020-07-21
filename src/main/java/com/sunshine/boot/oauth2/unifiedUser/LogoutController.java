package com.sunshine.boot.oauth2.unifiedUser;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.smartframework.common.utils.ExRedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dragonsoft.dcuc.client.logout.DcucLogoutHandler;
import com.dragonsoft.dcuc.common.util.PropUtils;
import com.sunshine.boot.oauth2.dao.UserDaoRepos;

@RestController
@RequestMapping("/api/sso")
public class LogoutController {
	
	@Autowired
	private UserDaoRepos userDaoRepos;

	 @RequestMapping("/logout")
	    public void logout(HttpServletRequest request, HttpServletResponse response){
	        if (!isAjaxRequest(request)){
	            //退出后重定向的地址,必须包含ip、Port和上下文的完整路径（如http://127.0.0.1:8080/ssotest/）
	            String redirectUrl = request.getParameter("redirectUrl");
	            if (StringUtils.isEmpty(redirectUrl)){
	                redirectUrl = PropUtils.getInstance().getConfigItem("dcuc.serverName");
	            }
	            //单点登录退出
	            System.out.println("返回地址====111========="+redirectUrl);
	            DcucLogoutHandler.logout(request, response, redirectUrl);
//	            return null;
	        } else {
	            //清除session
	            HttpSession session = request.getSession();
	            String userId = request.getSession().getAttribute("securityUser")!=null?request.getSession().getAttribute("securityUser").toString():null;
	            Map<String,Object> params = new HashMap<>();
	            params.put("title", "退出记录");
	            params.put("typename", "退出");
	        	params.put("cznr", "操作结果:退出成功");
	            insetLog(request,params,userId);
	            if (session != null) {
	                session.invalidate();
	            }
	            System.out.println("调用==========logout===========");
	            //拼装单点登录退出方法给前端
	            String logoutUrl = PropUtils.getInstance().getConfigItem("dcuc.casServerUrlPrefix") + "/logout";
	            String redirectUrl = request.getParameter("redirectUrl");
	            if (StringUtils.isEmpty(redirectUrl)){
	                redirectUrl = PropUtils.getInstance().getConfigItem("dcuc.serverName");
	            }
	            logoutUrl = logoutUrl+"?service="+redirectUrl;
	            System.out.println("返回地址====222========="+logoutUrl);
	            try {
					response.sendRedirect(logoutUrl);
				} catch (IOException e) {
					e.printStackTrace();
				}
//	            return logoutUrl;
	        }
	    }

	    public boolean isAjaxRequest(HttpServletRequest request){
	        //判断是否为ajax请求
	        String requestType = request.getHeader("X-Requested-With");
	        if ("XMLHttpRequest".equals(requestType)){
	            return true;
	        }
	        return false;
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
//				return userInfo;
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
}
