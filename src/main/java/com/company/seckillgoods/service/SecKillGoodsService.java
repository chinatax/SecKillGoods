package com.company.seckillgoods.service;

import com.company.seckillgoods.pojo.TbSeckillGoods;
import com.company.seckillgoods.pojo.common.Result;

import java.util.List;

/**
 * @description: 秒杀商品service
 * @author: chunguang.yao
 * @date: 2019-09-05 0:11
 */
public interface SecKillGoodsService {

    /**
     * 查询所有秒杀商品
     * @return
     */
    List<TbSeckillGoods> findAll();

    /**
     * 根据主键查询商品信息
     * @param id
     * @return
     */
    TbSeckillGoods findOne(Long id);

    /**
     * 保存订单
     * @param id
     * @param userId
     * @return
     */
    Result saveOrder(Long id, String userId);
}
