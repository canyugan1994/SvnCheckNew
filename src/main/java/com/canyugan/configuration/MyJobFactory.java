package com.canyugan.configuration;

import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.scheduling.quartz.AdaptableJobFactory;
import org.springframework.stereotype.Component;
/**
 * 自定义job工厂
 * @author caorui
 *
 */
@Component
public class MyJobFactory extends AdaptableJobFactory 
{
    //Spring自动注入
    @Autowired
    private AutowireCapableBeanFactory capableBeanFactory;
    
    @Override
    protected Object createJobInstance(TriggerFiredBundle bundle) throws Exception 
    {
        //调用父类的方法
        Object jobInstance = super.createJobInstance(bundle);
        //进行注入
        capableBeanFactory.autowireBean(jobInstance);
        return jobInstance;
    }
}