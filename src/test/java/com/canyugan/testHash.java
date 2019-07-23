package com.canyugan;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.canyugan.util.DateUtil;

import redis.clients.jedis.Jedis;

public class testHash 
{
	public static void main(String[] args) 
	{
		
		Map<String,String> all_project_needMove = new HashMap<String, String>();
		all_project_needMove.put("PJ-LX-Yunshang-2019-001","123");
		all_project_needMove.put("PJ-LX-Yunshang-2019-002","1234");
		all_project_needMove.put("PJ-LX-Yunshang-2019-003","12345");
		
		Map<String, String> hash_project_migration = new HashMap<String, String>();
		Set<String> p_key = all_project_needMove.keySet();
		Iterator<String> iterator_key = p_key.iterator();
		while(iterator_key.hasNext()) {
			hash_project_migration.put(iterator_key.next(), "1");
		}
		String migration_key = new DateUtil().currentDateTime() + "A167347";
		Jedis jedis = new Jedis("172.16.209.174", 16378);
		String resp = jedis.hmset(migration_key, hash_project_migration);//设置每个项目的初始迁移状态
		System.out.println(resp);
		
		//查看
		Map<String, String> all_result = jedis.hgetAll(migration_key);
		for(String key:all_result.keySet()) {
			System.out.println(key + " : " + all_result.get(key));
		}
		//jedis.hset("2019-07-22 12:13:04A167347", "PJ-LX-Yunshang-2019-002", "2");
	}
}
