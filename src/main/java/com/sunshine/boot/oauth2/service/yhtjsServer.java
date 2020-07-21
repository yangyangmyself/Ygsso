package com.sunshine.boot.oauth2.service;

import java.util.List;
import java.util.Map;

public interface yhtjsServer {
	int zrfwl();// 昨日访问量

	Map fwlzs();// 访问总量

	int yhzxxz(String yhdh);// 在线新增

	int yhlxtc(String yhdh);// 离线退出

	Map yhzxs();// 用户在线数

	Map yhsqs();// 用户总授权数

	Map yhzdls();// 用户总登录数

	int dl(Map map, String yhm);

	int tc(Map map);

	Map yhdlqcx(String yhdh);// 保存登陆前查询用户所在地

	List isShow(String yd);
}
