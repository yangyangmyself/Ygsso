package com.sunshine.boot.oauth2.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartframework.common.utils.ExRedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.sunshine.boot.oauth2.dao.PermissionDao;
import com.sunshine.boot.oauth2.dao.UserDao;

/**
 * @author oy
 * 
 */
@Service("scUserDetailsService")
public class ScUserDetailsService implements UserDetailsService {

	protected Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private UserDao userDao;
	@Autowired
	private yhtjsServerimpl yhm;
	@Autowired
	private PermissionDao permissionDao;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Map map = userDao.findByUserName(username);
		ExRedisUtils.put("Username", username);
		log.debug("UsernameSC" + username);
		if (map != null) {
			List permissions = getAuthorities(map.get("yhdh").toString());
			log.debug("UsernameSSO" + username);
			log.info("UsernameSSO" + username);
			ExRedisUtils.put("User:" + change(new Date()) + ":" + username, username);
			ExRedisUtils.expire("User:" + change(new Date()) + ":" + username, 14400);
			ExRedisUtils.put("UsersjbSCU", username);
			try{				
			Map user=yhm.yhdlqcx(username);
			String ip="登陆待完善，获取request";
			user.put("ip", ip);
			yhm.dl(user,null);
			}catch (Exception e) {
		log.error("用户登录成功记录出错============================"+e.getMessage());	
			}
			return new User(map.get("yhdh").toString(), map.get("mm").toString(), permissions);
		} 
			throw new UsernameNotFoundException("admin: " + username + " do not exist!");
		}	

	public static String change(Date d) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(d);

	}

	private List<GrantedAuthority> getAuthorities(String yhdh) {
		List permissions = permissionDao.findPermissionById(yhdh);
		List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
		for (Object permission : permissions) {
			Map map = (Map) permission;
			if (permission != null && map.get("role_name") != null) {
				// ROLE_ADMIN
				GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(
						"ROLE_" + map.get("role_name").toString());
				// 1：此处将权限信息添加到 GrantedAuthority
				// 对象中，在后面进行全权限验证时会使用GrantedAuthority对象
				grantedAuthorities.add(grantedAuthority);
			}
		}
		return grantedAuthorities;
	}
}
