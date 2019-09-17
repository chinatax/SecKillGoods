# SecKillGoods


- 秒杀商品系统【多线程处理】
- [需求分析](#需求分析)
- [技术架构](#技术架构)
- [秒杀实现思路](#秒杀实现思路)
- [数据库环境准备](#数据库环境准备)
- [导入秒杀商品到缓存](#导入秒杀商品到缓存)
- [秒杀下单](#秒杀下单)
- [超卖问题解决](#超卖问题解决)
- [并发问题解决](#并发问题解决)

### 需求分析
- 所谓“秒杀”，就是网络卖家发布一些超低价格的商品，所有买家在同一时间网上抢购的一种销售方式。通俗一点讲就是网络商家为促销等目的组织的网上限时抢购活动。由于商品价格低廉，往往一上架就被抢购一空，有时只用一秒钟。这就会导致系统短时间会遭受巨大的压力。
- 秒杀商品通常有两种限制：库存限制、时间限制。

### 技术架构
- 项目采用SSM架构，前端使用angularjs
- 数据库采用mysql
- 使用redis缓存
- 使用多线程解决并发问题


### 秒杀实现思路
- 秒杀技术实现核心思想是运用缓存减少数据库瞬间的访问压力！读取商品详细信息时运用缓存，当用户点击抢购时减少缓存中的库存数量，当库存数为0时或活动期结束时，同步到数据库。 产生的秒杀预订单也不会立刻写到数据库中，而是先写到缓存，当用户付款成功后再写入数据库。

![](https://i.imgur.com/5QmKyn3.png)

### 数据库环境准备

- Tb_seckill_goods 秒杀商品表
![](https://i.imgur.com/rcnz8gv.png)

- Tb_seckill_order 秒杀订单表

![](https://i.imgur.com/j2WBOUo.png)

- 建表sql
- `-- ----------------------------
-- Table structure for `tb_seckill_goods`
-- ----------------------------
DROP TABLE IF EXISTS `tb_seckill_goods`;
CREATE TABLE `tb_seckill_goods` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `goods_id` bigint(20) DEFAULT NULL COMMENT 'spu ID',
  `item_id` bigint(20) DEFAULT NULL COMMENT 'sku ID',
  `title` varchar(100) DEFAULT NULL COMMENT '标题',
  `small_pic` varchar(150) DEFAULT NULL COMMENT '商品图片',
  `price` decimal(10,2) DEFAULT NULL COMMENT '原价格',
  `cost_price` decimal(10,2) DEFAULT NULL COMMENT '秒杀价格',
  `seller_id` varchar(100) DEFAULT NULL COMMENT '商家ID',
  `create_time` datetime DEFAULT NULL COMMENT '添加日期',
  `check_time` datetime DEFAULT NULL COMMENT '审核日期',
  `status` varchar(1) DEFAULT NULL COMMENT '审核状态',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `num` int(11) DEFAULT NULL COMMENT '秒杀商品数',
  `stock_count` int(11) DEFAULT NULL COMMENT '剩余库存数',
  `introduction` varchar(2000) DEFAULT NULL COMMENT '描述',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of tb_seckill_goods
-- ----------------------------
INSERT INTO `tb_seckill_goods` VALUES (1, 149187842867960, NULL, '秒杀精品女装', 'http://img.mp.itc.cn/upload/20160804/6881885758bb42e09bff6e3d60d18230_th.jpg', 100.00, 0.01, 'qiandu', NULL, '2017-10-14 21:07:51', '1', '2017-10-14 18:07:27', '2017-10-14 18:07:31', 10, 5, NULL);
INSERT INTO `tb_seckill_goods` VALUES (2, 149187842867953, NULL, '轻轻奶茶', 'http://sem.g3img.com/site/50021489/image/c2_20190411232047_66099.jpg', 10.00, 0.01, 'yijia', NULL, NULL, '1', '2017-10-12 18:24:18', '2017-10-28 18:24:20', 10, 5, '清仓打折');
INSERT INTO `tb_seckill_goods` VALUES (3, 11, NULL, '11', 'http://i2.sinaimg.cn/ty/2014/0326/U5295P6DT20140326155117.jpg', 44.00, 0.03, NULL, NULL, NULL, '1', '2017-1-1 00:00:00', '2017-12-1 00:00:00', 10, 2, NULL);
INSERT INTO `tb_seckill_goods` VALUES (4, 2, NULL, '测试', 'http://www.cnr.cn/junshi/ztl/leifeng/smlf/201202/W020120226838451234901.jpg', 10.00, 0.01, 'qiandu', '2017-10-14 19:18:18', NULL, '0', '2017-11-11 00:00:00', '2017-11-11 23:59:59', 100, 99, NULL);
INSERT INTO `tb_seckill_goods` VALUES (5, NULL, NULL, '羽绒服', 'http://img14.360buyimg.com/popWaterMark/g13/M03/0A/1D/rBEhU1Kmlr8IAAAAAATBCejgYvoAAGmMAC0zhIABMEh349.jpg', 100.00, 0.02, 'qiandu', '2017-10-15 09:50:52', '2017-10-15 10:06:27', '1', '2017-10-10 00:00:00', '2017-11-11 23:59:59', 10, 10, '清仓打折');

-- ----------------------------
-- Table structure for `tb_seckill_order`
-- ----------------------------
DROP TABLE IF EXISTS `tb_seckill_order`;
CREATE TABLE `tb_seckill_order` (
  `id` bigint(20) NOT NULL COMMENT '主键',
  `seckill_id` bigint(20) DEFAULT NULL COMMENT '秒杀商品ID',
  `money` decimal(10,2) DEFAULT NULL COMMENT '支付金额',
  `user_id` varchar(50) DEFAULT NULL COMMENT '用户',
  `seller_id` varchar(50) DEFAULT NULL COMMENT '商家',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `status` varchar(1) DEFAULT NULL COMMENT '状态',
  `receiver_address` varchar(200) DEFAULT NULL COMMENT '收货人地址',
  `receiver_mobile` varchar(20) DEFAULT NULL COMMENT '收货人电话',
  `receiver` varchar(20) DEFAULT NULL COMMENT '收货人',
  `transaction_id` varchar(30) DEFAULT NULL COMMENT '交易流水',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of tb_seckill_order
-- ----------------------------
INSERT INTO `tb_seckill_order` VALUES ('919473120379723776', null, '0.02', 'lijialong', 'qiandu', '2017-10-15 16:00:49', '2017-10-15 16:03:36', '1', null, null, null, '4200000013201710158227452548');
INSERT INTO `tb_seckill_order` VALUES ('919474775091339264', null, '0.02', 'lijialong', 'qiandu', '2017-10-15 16:07:24', '2017-10-15 16:07:58', '1', null, null, null, '4200000007201710158230411417');
INSERT INTO `tb_seckill_order` VALUES ('919497114331951104', '2', '0.01', null, 'yijia', '2017-10-15 17:36:10', '2017-10-15 17:37:35', '1', null, null, null, '4200000004201710158248971034');
INSERT INTO `tb_seckill_order` VALUES ('919497943340302336', '2', '0.01', null, 'yijia', '2017-10-15 17:39:27', '2017-10-15 17:39:49', '1', null, null, null, '4200000011201710158245347392');
`

### 导入秒杀商品到缓存
- 秒杀查询压力是非常大的，我们可以在秒杀之前把秒杀商品存入到Redis缓存中，页面每次列表查询的时候直接从Redis缓存中取，这样会大大减轻MySQL数据库的压力。我们可以创建一个定时任务工程，每天秒杀的前一天运行并加载MySQL数据库数据到Redis缓存。在这里，使用了Quartz将数据定时导入数据库

### 秒杀下单
- 商品详细页点击立即抢购实现秒杀下单，下单时扣减库存。当库存为0或不在活动期范围内时无法秒杀。
- 秒杀下单业务流程：
- 1），从redis服务器中获取入库的秒杀商品
- 2），判断商品是否存在，或是是商品库存是否小于等于0
- 3），如果秒杀商品存在，创建秒杀订单
- 4），把新增订单存储在redis服务器中
- 5），把存储在redis中入库的商品库存减一
- 6），判断库存是否小于0,卖完需要同步数据库
- 7），否则同步redis购物车数量

![](https://i.imgur.com/ndGRaLE.png)


### 超卖问题解决
- 上面保存订单的方式是先查看Redis中对应商品是否存在，如果存在且数量是否>0如果>0则下单，如果在并发情况下,如果20个人同时在执行如上查询代码这里，而此时对应商品只有一个，则会下20个单，而这20个单一定是有问题的，因为1件商品不可能同时给20个人发货。那么如何解决这种并发问题呢？我们可以用Redis队列（list队列）实现。修改下单逻辑：除了原来在缓存中存储秒杀商品之外，还在list队列中存储一份（这个队列中的数据，可以通过固定的前缀+商品id作为key来存储商品的id；比如下图：
![](https://i.imgur.com/ILOJK4Y.png)
- goodsId为123的商品的库存数量为2，那么在队列中就存入两个该商品。
- 那么用户在下单的时候，不是直接从redis的缓存中获取商品，而是根据商品的id去队列中去获取，因为redis的队列的特点是多个线程去获取队列值的时候，队列中有多少个值，就有多少个线程能拿到这个值。比如上图中，队列中有两个值，那么就只有两个线程能拿到这个值。
![](https://i.imgur.com/BYWYo0l.png)

### 并发问题解决
- 问题分析
![](https://i.imgur.com/ErOGNJZ.png)
- 当用户秒杀成功，去创建订单的时候就会执行上面的这段代码，这段代码的逻辑还是比较复杂的，执行时间还是比较长的。那么这个时候为了加快响应速度，提高我们代码处理效率，我们使用多线程把这段修改成异步处理。
- 那么在下单的时候，我们就不是先判断是否有库存，而是先判断用户之前是不是已经买过或者抢购过该商品了，那么我们就需要记录之前用户是否已经抢购过该商品。那么我们就创建一个set集合来存放当前抢购的集合。
- 超卖问题解决的方案解决了并发情况下下单操作异常问题，但其实际秒杀中大量并发情况下，这个下单过程是需要很长等待时间的，所以这里我们建议用异步和多线程实现，最好不要让程序处于阻塞状态，而是在用户一下单的时候确认用户是否符合下单条件，如果符合，则开启线程执行，执行完毕之后，用户等待查询结果即可。
![](https://i.imgur.com/nNFRpkZ.png)