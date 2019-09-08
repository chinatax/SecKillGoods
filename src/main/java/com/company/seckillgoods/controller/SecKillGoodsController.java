package com.company.seckillgoods.controller;

import com.company.seckillgoods.pojo.TbSeckillGoods;
import com.company.seckillgoods.service.SecKillGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @description: 秒杀商品controller
 * @author: chunguang.yao
 * @date: 2019-09-05 0:11
 */
@RestController
@RequestMapping("/secKillGoodsController")
public class SecKillGoodsController {

    @Autowired
    private SecKillGoodsService secKillGoodsService;

    @RequestMapping("/findAll")
    private List<TbSeckillGoods> findAll() {
        return secKillGoodsService.findAll();
    }
}
