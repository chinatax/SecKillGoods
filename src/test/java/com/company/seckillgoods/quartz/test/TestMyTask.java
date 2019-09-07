package com.company.seckillgoods.quartz.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

/**
 * @description: 测试类
 * @author: chunguang.yao
 * @date: 2019-09-07 22:51
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring/applicationContext-task.xml")
public class TestMyTask {

    @Test
    public void test() throws IOException {
        // 为了保证启动之后容器不会关闭，使用while循环
        while (true) {
            // 在循环中接收键盘输入，就会阻塞
            System.in.read();
        }
    }
}
