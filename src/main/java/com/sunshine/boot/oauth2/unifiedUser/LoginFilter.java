package com.sunshine.boot.oauth2.unifiedUser;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.dragonsoft.dcuc.client.validation.DcucTicketValidationFilter;
import com.dragonsoft.dcuc.common.util.HttpUtils;
import com.dragonsoft.dcuc.common.util.PropUtils;
import com.sunshine.boot.oauth2.dao.UserDaoRepos;

import org.jasig.cas.client.validation.Assertion;
import org.smartframework.common.utils.ExHttpClientUtils;
import org.smartframework.common.utils.ExRedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class LoginFilter extends DcucTicketValidationFilter {

	@Autowired
	private UserDaoRepos userDaoRepos;
	
    /**
     * 单点登录登录后的回调
     * @param request
     * @param response
     * @param assertion
     * @return 返回要回调的前端地址（默认为sevice的地址）
     */
    @Override
    protected String onSuccessfulValidation(HttpServletRequest request, HttpServletResponse response, Assertion assertion) {
        //获取登录用户的userId
        String userId = assertion.getPrincipal().getName();
        Map<String, Object> infoMap = assertion.getPrincipal().getAttributes();
        //获取身份证号
        String gmsfzh = (String) infoMap.get("gmsfzh");
        //获取姓名
        String name = (String) infoMap.get("name");
        //获取警号
        String policeNumber = (String) infoMap.get("policeNumber");
        //获取警种
        String policeCategory = (String) infoMap.get("policeCategory");
        //获取业务域
        String policeBusiness = (String) infoMap.get("policeBusiness");
        //获取机构code
        String orgCode = (String) infoMap.get("orgCode");
        //获取机构名称
        String orgName = (String) infoMap.get("orgName");

        //获取角色代码（多个用“，”隔开）
        String roles = HttpUtils.getInstance().getUserRoles(userId);
        //获取权限菜单（多个用“，”隔开）
        String munes = HttpUtils.getInstance().getUserMenus(userId);
        //获取角色详情(roleCode、roleName)
        String  roleDetails = HttpUtils.getInstance().getUserRolesDetails(userId);
        //获取权限菜单详情(name、code、url、showMode{0:隐藏,1显示}、isActive{0:无效,1有效})
        String  menuDetails =  HttpUtils.getInstance().getUserMenusDetails(userId);
        //将用户信息缓存到session中，避免多次请求单点登录获取用户信息
        request.getSession().setAttribute("securityUser",userId);
        Cookie cookie = new Cookie("YgNetLoginToken",userId);
        cookie.setPath("/");
//        cookie.setDomain("/#");
        cookie.setMaxAge(28800);
        response.addCookie(cookie);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  //yyyy-MM-dd HH:mm:ss yyyyMM
		Date date = new Date();
        Map<String, Object> userInfo = new HashMap<String, Object>();
        if(name== null || "".equals(name)){
        	userInfo.put("yhmc", ".");
        } else {
        	userInfo.put("yhmc", name);
        }
        if(policeNumber == null || "".equals(policeNumber)){
        	userInfo.put("jh", ".");
        } else {
        	userInfo.put("jh", policeNumber);
        }
        if(userId== null || "".equals(userId)){
        	userInfo.put("userToken", ".");
        } else {
        	userInfo.put("userToken", userId);
        }
        if(orgName== null || "".equals(orgName)){
        	userInfo.put("bmmc", ".");
        } else {
        	userInfo.put("bmmc", orgName);
        }
        if(gmsfzh== null || "".equals(gmsfzh)){
        	userInfo.put("sfzmhm", ".");
        } else {
        	userInfo.put("sfzmhm", gmsfzh);
        }
        if(name== null || "".equals(name)){
        	userInfo.put("sex", ".");
        } else {
        	userInfo.put("sex", "1");
        }
        userInfo.put("dlsj", sdf.format(date));
        if(name== null || "".equals(name)){
        	userInfo.put("ulxdh1", ".");
        } else {
        	userInfo.put("ulxdh1", ".");
        }
        if(userId== null || "".equals(userId)){
        	userInfo.put("username", name);
        } else {
        	if(policeNumber == null || "".equals(policeNumber)){
            	userInfo.put("username", ".");
            } else {
            	userInfo.put("username", policeNumber);
            }
        }
//        ExRedisUtils.put("archive-token",userInfo,3600);
        
        System.out.println("onSuccessfulValidation==========用户信息"+userInfo);
        System.out.println("onSuccessfulValidation==========userId"+userId);
        ExRedisUtils.putMap(userId, userInfo, 28800);
//        ExRedisUtils.putMap(userId, userInfo, 1);
//        ExRedisUtils.hmset(userId, userInfo);
        /*String str = request.getRequestURL().toString();
        String str1 = request.getRequestURI();
        String requestUrl = str.substring(0, str.indexOf(str1)) + "/ssov6/user/get/info";
        System.out.println("requestUrl======"+requestUrl);
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("userinfo_token", userId);
        String value = ExHttpClientUtils.doPost(requestUrl, paramMap);
        System.out.println("value======="+value);
        Map<String, Object> userMap = JSONObject.parseObject(value, new TypeReference<Map<String, Object>>(){});*/
        //返回前端地址
        return PropUtils.getInstance().getConfigItem("dcuc.serverName");
//        return "http://localhost:8066/ssov6/login/verification.html?phone="+userMap.get("ulxdh1");
    }
}
