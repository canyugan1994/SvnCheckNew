package com.canyugan.configuration;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

/**
 * 定时调度器
 * 同步双速项目信息到数据库
 * @author caorui
 */
@Configuration
public class QuartzSynchProject 
{
	@Value("${quartz_period}")
	private String quartz_period;
	@Autowired
	private MyJobFactory jobFactory;
	
	@Bean
    public SchedulerFactoryBean schedulerFactoryBean() {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        //service注入问题
        factory.setJobFactory(jobFactory);
        return factory;
	}
	
	@Bean
	public Scheduler scheduler() throws SchedulerException 
	{
		Scheduler scheduler = schedulerFactoryBean().getScheduler();
		JobDetail jb = JobBuilder.newJob(ProjectSyncJob.class)
                .withDescription("同步项目信息每周一定期调度") //job的描述
                .withIdentity("项目信息同步", "sx") //job 的name和group
                .build();
		
		TriggerBuilder<Trigger> newTrigger = trigger();
		Trigger t = newTrigger.withDescription("")
	         .withIdentity("spliderTrigger", "spliderTriggerGroup")
	         .startNow()
	         .withSchedule(CronScheduleBuilder.cronSchedule(quartz_period)) //每隔30分钟执行一次
	         .build();
		
		scheduler.scheduleJob(jb, t);
		scheduler.start();
		return scheduler;
	}
	
	@Bean
	public TriggerBuilder<Trigger> trigger() {
		return TriggerBuilder.newTrigger();
	}
}
