package com.company.seckillgoods.service.impl;

import com.company.seckillgoods.common.GlobalContant;
import com.company.seckillgoods.mapper.TbSeckillGoodsMapper;
import com.company.seckillgoods.pojo.TbSeckillGoods;
import com.company.seckillgoods.pojo.common.OrderRecord;
import com.company.seckillgoods.pojo.common.Result;
import com.company.seckillgoods.service.SecKillGoodsService;
import com.company.seckillgoods.thread.CreateOrderThread;
import com.company.seckillgoods.util.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @description: 秒杀商品service实现类
 * @author: chunguang.yao
 * @date: 2019-09-05 0:11
 */
@Service
@Transactional
public class SecKillGoodsServiceImpl implements SecKillGoodsService {

    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    // 注入id生成工具
    @Autowired
    private IdWorker idWorker;

    @Resource
    private ThreadPoolTaskExecutor executor;

    @Resource
    private CreateOrderThread createOrderThread;

    public List<TbSeckillGoods> findAll() {
        return seckillGoodsMapper.selectByExample(null);
    }

    @Override
    public TbSeckillGoods findOne(Long id) {
        return seckillGoodsMapper.selectByPrimaryKey(id);
    }

    @Override
    public Result saveOrder(Long id, String userId) {
        //0.从用户的set集合中判断用户是否已下单
        Boolean member = redisTemplate.boundSetOps(GlobalContant.USER_ID_PREFIX + id).isMember(userId);
        if(member) {
            //如果正在排队或者未支付的，提示用户你正在排队或有订单未支付
            return new Result(false, "对不起，您正在排队等待支付，请尽快支付！");
        }
        // 1、从redis的队列中获取秒杀商品id
        Long goodsId = (Long) redisTemplate.boundListOps(GlobalContant.SECKILLGOODS_ID_PREFIX + id).rightPop();
        // 2、判断商品是否存在
        if(null == goodsId) {
            // 3、商品不存在，或者库存 <= 0 ，返回失败，提示已售罄
            return  new Result(false, "该商品已售罄，请您查看其他商品!");
        }
        //4.将用户放入用户集合
        redisTemplate.boundSetOps(GlobalContant.USER_ID_PREFIX + id).add(userId);
        //5.创建OrderRecord对象记录用户下单信息：用户id，商品id，放到OrderRecord队列中
        OrderRecord orderRecord = new OrderRecord(id, userId);
        redisTemplate.boundListOps(OrderRecord.class.getSimpleName()).leftPush(orderRecord);
        //6.通过线程池启动线程处理OrderRecord中的数据，返回成功
        executor.execute(createOrderThread);
        return new Result(true, "秒杀成功，请您尽快支付!");
    }
}
