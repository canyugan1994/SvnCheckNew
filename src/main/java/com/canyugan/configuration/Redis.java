package com.canyugan.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * redis连接池配置
 * @author caorui
 */
@Configuration
@EnableCaching
public class Redis 
{
	private static final Logger LOG = LoggerFactory.getLogger(Redis.class);
	
	@Bean
	@Conditional(RedisPoolConditional.class)
	public JedisPool redisPool(@Value("${redis.host}") String host,
							  @Value("${redis.port}") String port,
							  @Value("${redis.timeout}") int timeout,
							  @Value("${redis.pool.max-idle}")  int maxIdle,
							  @Value("${redis.pool.min-idle}") int minIdle,
							  @Value("${redis.pool.max-wait}") long maxWaitMillis)
	{
		JedisPoolConfig jedisPoolConfig = null;
		JedisPool jedisPool = null;
		try {
			jedisPoolConfig = new JedisPoolConfig();
			jedisPoolConfig.setMaxTotal(2000);
			jedisPoolConfig.setMaxIdle(maxIdle);
			jedisPoolConfig.setMinIdle(minIdle);
			jedisPoolConfig.setMaxWaitMillis(maxWaitMillis);

			jedisPool = new JedisPool(jedisPoolConfig, host, Integer.valueOf(port),1000);
			LOG.info("-->【 Jedis连接池初始化成功, ping：" + jedisPool.getResource().ping() + "】");
		} catch (Exception e) {
			LOG.info("-->【 Jedis连接池初始化失败 】");
		}
		return jedisPool;
	}
}
