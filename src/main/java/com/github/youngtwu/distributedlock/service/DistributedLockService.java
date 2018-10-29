package com.github.youngtwu.distributedlock.service;

import com.github.youngtwu.distributedlock.commonUtils.DistributedLock;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @Auther: ASUS
 * @Date: 2018/10/29 16:07
 * @Description: 分布式锁服务类
 */
public class DistributedLockService {
    private static JedisPool jedisPool = null;

    private DistributedLock distributedLock = new DistributedLock(jedisPool);

    int n = 500;

    static {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        //设置最大连接数
        jedisPoolConfig.setMaxTotal(200);
        //设置最大空闲数
        jedisPoolConfig.setMaxIdle(8);
        //设置最大等待时间
        jedisPoolConfig.setMaxWaitMillis(1000*100);
        //在borrow一个jedis实例时，是否需要验证，若为true，则所有jedis实例均是可用的
        jedisPoolConfig.setTestOnBorrow(true);
        jedisPool = new JedisPool(jedisPoolConfig, "127.0.0.1", 6379, 3000);
    }


    public void seckill() {
        // 返回锁的value值，供释放锁时候进行判断
        String identifier = distributedLock.lockWithTimeout("resource", 5000, 1000);
        System.out.println(Thread.currentThread().getName() + "获得了锁");
        System.out.println(--n);
        distributedLock.releaseLock("resource", identifier);
    }
}
