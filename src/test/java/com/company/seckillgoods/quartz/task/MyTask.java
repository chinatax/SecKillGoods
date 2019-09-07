package com.company.seckillgoods.quartz.task;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @description: 测试Quartz的任务类
 * @author: chunguang.yao
 * @date: 2019-09-07 22:45
 */
@Component
public class MyTask {

    /**
     * 每5秒执行一次
     */
    @Scheduled(cron = "*/5 * * * * ?")
    public void excTask() {
        System.out.println("Quartz执行定时任务,当前时间为: " + new Date());
    }
}
