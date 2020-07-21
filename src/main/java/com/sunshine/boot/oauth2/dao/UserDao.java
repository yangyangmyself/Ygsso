package com.sunshine.boot.oauth2.dao;

import java.util.Map;

public interface UserDao {
	
	public Map findByUserName(String yhdh);


	public Map findBySFZ(String sfz);
	
	public int insertLong(Map<String,Object> params);
}