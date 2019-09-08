package com.company.seckillgoods.service;

import com.company.seckillgoods.pojo.TbSeckillGoods;

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
}
