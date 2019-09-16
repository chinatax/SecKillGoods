package com.company.seckillgoods.pojo.common;

import java.io.Serializable;

/**
 * @description: 创建订单类
 * @author: chunguang.yao
 * @date: 2019-09-17 0:04
 */
public class OrderRecord implements Serializable {

    /** 商品id */
    private Long id;

    /** 用户id */
    private String userId;

    public OrderRecord(Long id, String userId) {
        this.id = id;
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
