package com.sunshine.boot.oauth2.dao;

import java.util.List;

import org.springframework.stereotype.Repository;
@Repository
public class PermissionDaoRepos extends AbstractRepos implements PermissionDao {

	public PermissionDaoRepos() {
		this("T_SYS_SYSUSER_ROLE", "id");
	}
	
	public PermissionDaoRepos(String tableName, String primaryKey) {
		super(tableName, primaryKey);
	}

	@Override
	public List findPermissionById(String yhdh) {
		String sql = "select * from T_SYS_ROLE where role_id in(select role_id from " + this.getMasterTable() + " where yhdh=?)";
		return this.queryDataBySQL(sql, new Object[]{yhdh});
	}

}
