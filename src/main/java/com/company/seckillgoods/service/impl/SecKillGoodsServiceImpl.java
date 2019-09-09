package com.company.seckillgoods.service.impl;

import com.company.seckillgoods.common.GlobalContant;
import com.company.seckillgoods.mapper.TbSeckillGoodsMapper;
import com.company.seckillgoods.pojo.TbSeckillGoods;
import com.company.seckillgoods.pojo.TbSeckillOrder;
import com.company.seckillgoods.pojo.common.Result;
import com.company.seckillgoods.service.SecKillGoodsService;
import com.company.seckillgoods.util.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
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

    public List<TbSeckillGoods> findAll() {
        return seckillGoodsMapper.selectByExample(null);
    }

    @Override
    public TbSeckillGoods findOne(Long id) {
        return seckillGoodsMapper.selectByPrimaryKey(id);
    }

    @Override
    public Result saveOrder(Long id, String userId) {
        // 1、从redis的队列中获取秒杀商品id
        Long goodsId = (Long) redisTemplate.boundListOps(GlobalContant.SECKILLGOODS_ID_PREFIX + id).rightPop();
        // 2、判断商品是否存在
        if(null == goodsId) {
            // 3、商品不存在，或者库存 <= 0 ，返回失败，提示已售罄
            return  new Result(false, "该商品已售罄，请您查看其他商品!");
        }
        // 能从队列中拿到id，说明用户秒杀成功，从redis缓存获取商品信息去创建订单。
        TbSeckillGoods tbSeckillGoods  = (TbSeckillGoods) redisTemplate.boundHashOps(TbSeckillGoods.class.getSimpleName()).get(id);
        // 4、生成秒杀订单，将订单保存到redis
        TbSeckillOrder seckillOrder = new TbSeckillOrder();
        seckillOrder.setUserId(userId);
        seckillOrder.setSellerId(tbSeckillGoods.getSellerId());
        // 设置一个全局唯一的秒杀id
        seckillOrder.setSeckillId(idWorker.nextId());
        seckillOrder.setMoney(tbSeckillGoods.getCostPrice());
        seckillOrder.setCreateTime(new Date());
        // 未支付
        seckillOrder.setStatus("0");
        redisTemplate.boundHashOps(TbSeckillOrder.class.getSimpleName()).put(userId, seckillOrder);
        // 5、秒杀商品库存量-1
        tbSeckillGoods.setStockCount(tbSeckillGoods.getStockCount() - 1);;
        // 6、判断库存量是否 <= 0
        if(tbSeckillGoods.getStockCount() <= 0) {
            // 7、是，将秒杀商品更新到数据库，删除redis对应的秒杀商品
            seckillGoodsMapper.updateByPrimaryKey(tbSeckillGoods);
            redisTemplate.boundHashOps(TbSeckillGoods.class.getSimpleName()).delete(id);
        } else {
            // 8、否，将秒杀商品更新到缓存，返回成功
            redisTemplate.boundHashOps(TbSeckillGoods.class.getSimpleName()).put(id, tbSeckillGoods);
        }
        return new Result(true, "秒杀成功，请您尽快支付!");
    }
}
