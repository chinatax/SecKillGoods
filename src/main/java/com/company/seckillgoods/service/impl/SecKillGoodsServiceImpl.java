package com.company.seckillgoods.service.impl;

import com.company.seckillgoods.mapper.TbSeckillGoodsMapper;
import com.company.seckillgoods.pojo.TbSeckillGoods;
import com.company.seckillgoods.service.SecKillGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public List<TbSeckillGoods> findAll() {
        return seckillGoodsMapper.selectByExample(null);
    }
}
