package com.company.seckillgoods.pojo.common;

/**
 * @description: 返回结果类
 * @author: chunguang.yao
 * @date: 2019-09-08 23:39
 */
public class Result {

    // 是否成功
    private Boolean success;

    // 返回信息
    private String message;

    public Result(Boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
