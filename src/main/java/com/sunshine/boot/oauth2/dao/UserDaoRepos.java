package com.sunshine.boot.oauth2.dao;

import org.springframework.stereotype.Repository;

import java.util.Map;
@Repository
public class UserDaoRepos extends AbstractRepos implements UserDao{



	public UserDaoRepos() {
		this("T_AC_SYSUSER", "yhdh");
	}
	
	public UserDaoRepos(String tableName, String primaryKey) {
		super(tableName, primaryKey);
	}

	@Override
	public Map findByUserName(String yhdh) {
		String sql = "select * from " + this.getMasterTable() + " where yhdh=? or sfzmhm=?";
		return findOneBySQL(sql, new Object[]{yhdh, yhdh});
	}

	@Override
	public Map findBySFZ(String sfz) {

		String sql ="select * from " + this.getMasterTable() + " where sfzmhm= ? ";
 		return findOneBySQL(sql,new Object[]{sfz});
	}

	@Override
	public int insertLong(Map<String, Object> params) {
		int result = 0;
		this.setMasterTable("t_sys_log");
		this.setMasterTablePK("id");
		result = Integer.valueOf(this.doInsert(params).toString());
		this.setMasterTable("T_AC_SYSUSER");
		this.setMasterTablePK("yhdh");
		return result;
	}


}
