package com.sky.tools;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DeleteHelper {

	public DeleteHelper() {
		super();
	}

	
	@Autowired
	private JdbcTemplate jdbc;
	
	public void doDelete(String sql) throws Exception{
		jdbc.execute(sql);
	}
	
	public void doDelete(String sql,List<Object[]> args) throws Exception{
		jdbc.batchUpdate(sql,args);
	}
	
	public void doDelete(String sql,Object[] args) throws Exception{
		jdbc.update(sql, args);
	}


}
