package com.sunshine.boot.oauth2.service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartframework.common.utils.ExDateUtils;
import org.smartframework.common.utils.ExRedisUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.sunshine.boot.oauth2.dao.yhtjsDao;

@Service("yhtjs")
public class yhtjsServerimpl implements yhtjsServer {
	
	 private static final Logger log = LoggerFactory.getLogger(yhtjsServerimpl.class);

	@Autowired
	private yhtjsDao yhdao;

	@Override
	public int zrfwl() {
		Date today = new Date();
		Date yesterday = new Date(today.getTime() - 86400000L);
		String date = change(yesterday);
		Map gg = yhdao.yhzfwl(date);
		if (gg.get("zs") == null || gg.get("zs").equals("")) {
			return 0;
		}
		return Integer.parseInt(String.valueOf(gg.get("zs")));
	}

	public static String change(Date d) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(d);

	}

	public static String change1(Date d) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		return sdf.format(d);

	}
	@Override
	public List isShow(String yd) {
		List map = yhdao.isShow(yd);
		return map;
	}
	@Override
	public Map fwlzs() {
		Map map = yhdao.yhzfwl(null);
		return map;
	}

	public Map kkzxs() {
		Map map = yhdao.kkzxs();
		return map;
	}
	@Override
	public int yhzxxz(String yhdh) {
		List list = yhdao.dlzxqcx(yhdh);
		if (list.size() > 0) {
			Date today = new Date();
			return yhdao.update(yhdh, change1(today));
		} else {
			return yhdao.dlxz(yhdh);
		}
	}
	@Override
	public int yhlxtc(String yhdh) {
		List list = null;
		list = yhdao.dlzxqcx(yhdh);
		if (list.size() > 0 && list != null) {
			return yhdao.tcsc(yhdh);
		} else {
			return 0;
		}
	}
	@Override
	public Map yhzxs() {
		return yhdao.yhzxs();
	}
	@Override
	public Map yhsqs() {
		return yhdao.yhsqs();
	}
	@SuppressWarnings("unchecked")
	@Override
	public int dl(Map map, String yhm) {
		Map pMap=new HashMap();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> total = new HashMap<String, Object>();
		pMap.put("type", 6);//类型为6 是登录
		 SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");     
         String key="ywsj:"+formatter.format(new Date())+"dls";
         log.debug("登入数"+key);
		ExRedisUtils.incr(key);
		ExRedisUtils.incr("ywsj:zdls");
		ExRedisUtils.exists(key);
		return yhdao.dl(map, yhm);
	}
	@Override
	public int tc(Map map) {
		return yhdao.tc(map);
	}
	@Override
	public Map yhzdls() {
		// TODO Auto-generated method stub
		return yhdao.yhzdls();
	}
	@Override
	public Map yhdlqcx(String yhdh) {
		return yhdao.yhdlqcx(yhdh);
	}

}
