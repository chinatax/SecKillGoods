package com.company.seckillgoods.quartz.task;

import com.company.seckillgoods.mapper.TbSeckillGoodsMapper;
import com.company.seckillgoods.pojo.TbSeckillGoods;
import com.company.seckillgoods.pojo.TbSeckillGoodsExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * @description:  通过定时任务导入mysql数据到redis
 * @author: chunguang.yao
 * @date: 2019-09-08 0:49
 */
@Component
public class SecKillGoodsToRedis {

    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 每10秒钟执行一次
     */
    @Scheduled(cron = "*/10 * * * *  ? ")
    public void loadDataToRedis() {
        // 查询出数据库中秒杀商品数据
        TbSeckillGoodsExample example = new TbSeckillGoodsExample();
        TbSeckillGoodsExample.Criteria criteria = example.createCriteria();
        // 符合要求：(1)状态(status)为1 (2)库存数量(stock_count)大于0
        // (3)活动开始时间(start_time)  <=  当前时间 < 活动结束时间(end_time)
        criteria.andStatusEqualTo("1");
        criteria.andStockCountGreaterThan(0);
        Date currentDate = new Date();
        criteria.andStartTimeLessThan(currentDate);
        criteria.andEndTimeGreaterThan(currentDate);
        List<TbSeckillGoods> seckillGoodsList = seckillGoodsMapper.selectByExample(example);
        // 将查询出来的秒杀商品导入到redis
        for (TbSeckillGoods goods : seckillGoodsList) {
            System.out.println(TbSeckillGoods.class.getSimpleName());
            // 将数据存入到redis的hash数据结构中(类似于map)
            redisTemplate.boundHashOps("TbSeckillGoods").put(goods.getId(), goods);
        }
    }
}
