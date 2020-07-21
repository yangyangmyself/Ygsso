package com.sunshine.boot.oauth2.dao;

import com.sunshine.common.repository.DataRepositoryJDBC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class yhtjsDaoRepos extends DataRepositoryJDBC implements yhtjsDao {
	
	 private static final Logger log = LoggerFactory.getLogger(yhtjsDaoRepos.class);

	@Override
	public int dlxz(String yhdh) {
		String sql = "insert into t_yp_yhzxb(name,sj)VALUES(?,now())";
		return this.doUpdate(sql, yhdh);
	}

	@Override
	public List dlzxqcx(String yhdh) {
		String sql = "select * from t_yp_yhzxb where name= ?";
		Object[] parm = new String[] { yhdh };
		return this.queryDataBySQL(sql, parm);
	}

	public List isShow(String yhdh) {
		String sql = "	select gm.menuOperation from "
				+ "t_sys_sysuser u ,t_sys_usergroupmember ug,t_sys_usergroup g,t_sys_usergroupright gm,t_sys_menu m where  "
				+ "u.yhdh = ug.loginid and ug.userGroupId = g.userGroupId and g.userGroupId = gm.usergroupId "
				+ "and gm.menuId = m.menuId  and u.yhdh =?  and m.menuId='294' and  gm.menuOperation is not null "
				+ "and g.isActive != 'N' ";
		Object[] parm = new String[] {yhdh};
		List list = new ArrayList();
		list = this.queryDataBySQL(sql, parm);
		return list;
	}

	@Override
	public int tcsc(String yhdh) {
		String sql = "DELETE from t_yp_yhzxb where name=?";
		Object[] parm = new String[] { yhdh };
		return this.doUpdate(sql, parm);
	}

	@Override
	public Map yhzfwl(String date) {
		Object[] parm = null;
		// String sql="select SUM(fwl) as zs from t_yp_fwlb where 1=1 ";
		String sql = "select count(1) as zs   from t_sys_log where 1=1 and menu2<>'' ";
		if (date != null && !date.equals("")) {
			// sql +=" and sj = ? ";
			sql += "  and cjsj = ? ";
			parm = new String[] { date };

		}
		return this.findOneBySQL(sql, parm);
	}

	@Override
	public int update(String yhdh, String sj) {
		Object[] parm = new String[] { sj, yhdh };
		String sql = "update t_yp_yhzxb set sj=?  where name=?";
		return this.doUpdate(sql, parm);
	}

	@Override
	public Map yhzxs() {
		String sql = "select count(*) as yhzxs from t_yp_yhzxb ";
//		Object[] parm = new String[] {};
		return this.findOneBySQL(sql, null);
	}

	@Override
	public Map kkzxs() {
		String sql = "select count(*) as kks from t_vs_gate ";
		return this.findOneBySQL(sql, null);
	}

	@Override
	public Map yhsqs() {
		String sql = "select sum(t1.loginCount) as loginCount from (select b1.userGroupId,count(DISTINCT b1.loginid) as loginCount from b_usergroupmember b1 left join b_usergroup b2 on b1.userGroupId=b2.userGroupId GROUP BY b1.userGroupId) t1 ";
		return this.findOneBySQL(sql, null);
	}

	@Override
	public int dl(Map map, String yhm) {
		Object[] parm = new String[] {};
		String ip="";
		String jh="";
		String cjr;
		String cjrmc;
		String city;
		String cjdw;
		String cjdwmc;
		String cznr;
		if (map.isEmpty()) {
			cjr = "";
			cjrmc = "";
			city = "";
			cjdw = "";
			cjdwmc = "";
			cznr = "操作结果：登录失败：" + yhm;
		} else {
			ip = map.get("ip").toString();
			cjr = map.get("cjr").toString();
			cjrmc = map.get("cjrmc").toString();
			city = map.get("city").toString();
			cjdw = map.get("cjdw").toString();
			cjdwmc = map.get("cjdwmc").toString();
			jh = map.get("jh").toString();
			cznr = "操作结果：登录成功";
			if (yhm != null) {
				cznr = "操作结果：登录失败：" + yhm;
			}
		}

//		String sql = "insert into t_sys_log(ip,title,sourcetype,sourcetypename,type,typename,cznr,cjr,cjrmc,city,cjdw,cjdwmc,cjsj,`status`,jh)";
//		sql += "values('"+ip+"','用户登录','0','电脑端','6','登录'";
//		sql += ",'" + cznr + "','" + cjr + "','" + cjrmc + "','" + city + "','" + cjdw + "'";
//		sql += ",'" + cjdwmc + "'";
//		sql += ",NOW(),'0','"+jh+"')";
//		String sql = "insert into t_sys_log(ip,title,sourcetype,sourcetypename,type,typename,cznr,cjr,cjrmc,city,cjdw,cjdwmc,cjsj,`status`,jh)";
//		sql += "values('?','用户登录','0','电脑端','6','登录'";
//		sql += ",'?','?','?','?','?'";
//		sql += ",'?'";
//		sql += ",NOW(),'0','?')";
		/*
		 * String
		 * sql="insert into t_sys_log(type,typename,cjsj,cznr,city)values('6','登录',NOW(),'操作结果：ok',?)"
		 * ;
		 */
		this.setMasterTable("t_sys_log");
		this.setMasterTablePK("id");
//		return this.doUpdate(sql, new Object[]{ip,cznr,cjr,cjrmc,city,cjdw,cjdwmc,jh});
		return Integer.parseInt(this.doInsert(map).toString());
	}
	
	//去日志表中查询所有的登录数
	
	@Override
	public Map<String,Object> getCountByType(Map<String,Object> params){
		Map<String,Object> map=null;
		List<String> param=new ArrayList<String>(); 
		String sql="select count(*) total from t_sys_log where 1=1 ";
		sql+=" and type= 6 ";
		//param.add(params.get("type").toString());
		map = this.findOneBySQL(sql, param.toArray());
		if(map.size()==0) {
			map.put("total", 0);
		}
		return map;
	}
	
	
	//把总登录数，今日登录数存进数据库中
	public int insertBusData(Map map) {
		 try {
			 String sql = "update t_sys_busdata set zdls = ? , jrdls = ?  where date like ? " ;
			    this.getJdbcTemplate().update(sql,new Object[]{map.get("zdls"),map.get("jrdls"),map.get("date")+"%"});
	            return 0;
	        } catch (Exception e) {
	        	log.error("更新数据失败："+e);
	            return 1;
	        }
		
	}

	@Override
	public int tc(Map map) {
		Object[] parm = new String[] {};
		String ip="";
		String cjr;
		String cjrmc;
		String city;
		String cjdw;
		String cjdwmc;
		String jh="";
		String cznr = "操作结果：退出成功";
		if (map.isEmpty()) {
			cjr = "";
			cjrmc = "";
			city = "";
			cjdw = "";
			cjdwmc = "";
			cznr = "操作结果：退出失败，没有获取到有效的用户标识";
		} else {
			ip = map.get("ip").toString();
			cjr = map.get("cjr").toString();
			cjrmc = map.get("cjrmc").toString();
			city = map.get("city").toString();
			cjdw = map.get("cjdw").toString();
			cjdwmc = map.get("cjdwmc").toString();
			jh = map.get("jh").toString();
			cznr = "操作结果：退出成功";
		}
//		String sql = "insert into t_sys_log(ip,title,sourcetype,sourcetypename,type,typename,cznr,cjr,cjrmc,city,cjdw,cjdwmc,cjsj,`status`,jh)";
		// sql+="values('192.168.110.131','退出记录','0','电脑端','7','退出','操作结果：ok'";
//		sql += "values('"+ip+"','退出记录','0','电脑端','7','退出'";
//		sql += ",'" + cznr + "','" + cjr + "','" + cjrmc + "','" + city + "','" + cjdw + "'";
//		sql += ",'" + cjdwmc + "'";
//		sql += ",NOW(),'0','"+jh+"')";
		/*
		 * String
		 * sql="insert into t_sys_log(type,typename,cjsj,cznr,city)values('6','登录',NOW(),'操作结果：ok',?)"
		 * ;
		 */
		this.setMasterTable("t_sys_log");
		this.setMasterTablePK("id");
//		return this.doUpdate(sql, new Object[]{ip,cznr,cjr,cjrmc,city,cjdw,cjdwmc,jh});
		return Integer.parseInt(this.doInsert(map).toString());
//		return this.doUpdate(sql, parm);
	}

	@Override
	public Map yhzdls() {
		String sql = "select count(*) as zdls from t_sys_log where type='6' ";
		return this.findOneBySQL(sql, null);
	}

	@Override
	public Map yhdlqcx(String yhdh) {
		Object[] parm = new String[] {yhdh, yhdh};
		String sql = "select v.bmmc cjdwmc, t.jh jh, t.yhdh cjr,t.yhmc cjrmc,t.jh,t.glbm cjdw,SUBSTR(t.glbm,1,6) as city from t_ac_sysuser t,t_vs_department v where t.glbm=v.glbm and (yhdh=? or sfzmhm=?)";
		return this.findOneBySQL(sql, parm);
	}

	@Override
	public int insertBusData2(Map map) {
		this.setMasterTable("t_sys_busdata");
//		this.setMasterTablePK("id");
//		return this.doUpdate(sql, new Object[]{ip,cznr,cjr,cjrmc,city,cjdw,cjdwmc,jh});
		return Integer.parseInt(this.doInsert(map).toString());
//		String sql = "INSERT into t_sys_busdata(jrbks,jrcxs,jrdls,zdls,`date`)"+
//				" VALUES(0,0,0,0,SUBSTRING(NOW(),1,10))";
//						int i = this.jdbcTemp.update(sql);
//						return i;
	}
}
