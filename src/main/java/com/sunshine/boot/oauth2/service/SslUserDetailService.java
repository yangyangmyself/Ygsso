package com.sunshine.boot.oauth2.service;

import com.sunshine.boot.oauth2.dao.PermissionDao;
import com.sunshine.boot.oauth2.dao.UserDao;
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
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 用于PKI登录
 */
@Service("SslUserDetailsService")
public class SslUserDetailService implements UserDetailsService {

    protected Logger log = LoggerFactory.getLogger(SslUserDetailService.class);

    @Autowired
    private UserDao userDao;

    @Autowired
    private PermissionDao permissionDao;

    @Override
    public UserDetails loadUserByUsername(String sfz) throws UsernameNotFoundException {
        Map map = userDao.findBySFZ(sfz);


        if (map.get("yhdh") != null) {
            //ExRedisUtils.put("Username", map.get("yhdh").toString());
            log.debug("UsernameSC"+map.get("yhdh").toString());
            List permissions = getAuthorities(map.get("yhdh").toString());
            //ExRedisUtils.put("User:"+change(new Date())+":"+map.get("yhdh").toString(),map.get("yhdh").toString());
            //ExRedisUtils.expire("User:"+change(new Date())+":"+map.get("yhdh"),3600);
            return new User(map.get("yhdh").toString(), map.get("mm")
                    .toString(), permissions);
        } else {
            throw new UsernameNotFoundException("admin sfz: " + sfz
                    + " do not exist!");
        }

    }

    public static String change(Date d){
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(d);

    }

    private List getAuthorities(String yhdh) {
        List permissions = permissionDao.findPermissionById(yhdh);
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        for (Object permission : permissions) {
            Map map = (Map)permission;
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
