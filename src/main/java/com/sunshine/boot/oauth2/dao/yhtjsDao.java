package com.sunshine.boot.oauth2.dao;

import java.util.List;
import java.util.Map;

public interface yhtjsDao {
	public Map yhzxs();// 用户在线数

	public int dlxz(String yhdh);// 登陆成功新增

	public List dlzxqcx(String yhdh);/* 新增前确认是否存在 */

	public int tcsc(String yhdh);// 退出删除

	public Map yhzfwl(String date);/* 总访问量 */

	public int update(String yhdh, String sj);// 用户存在 修改登录时间防止计划清除

	public Map kkzxs();

	Map yhzdls();// 用户总登录数

	Map yhsqs();

	int dl(Map map, String yhm);
	
	public Map<String,Object> getCountByType(Map<String,Object> params);
	
	public int insertBusData(Map map);
	public int insertBusData2(Map map);

	int tc(Map map);

	Map yhdlqcx(String yhdh);// 保存登陆前查询用户所在地

	public List isShow(String yd);
}
