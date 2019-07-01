package com.canyugan.configuration;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 判断配置文件中redis相关配置项是否填写
 * @author caorui
 *
 */
public class RedisPoolConditional implements Condition
{

	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata)
	{
		Environment env = context.getEnvironment();
		
		return env.containsProperty("redis.host")
			   && env.containsProperty("redis.port")
			   && env.containsProperty("redis.timeout")
			   && env.containsProperty("redis.pool.max-idle")
			   && env.containsProperty("redis.pool.min-idle")
			   && env.containsProperty("redis.pool.max-wait");
	}

}
