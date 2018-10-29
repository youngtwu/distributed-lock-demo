package com.github.youngtwu.distributedlock.commonUtils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.exceptions.JedisException;

import java.util.List;
import java.util.UUID;

/**
 * @Auther: ASUS
 * @Date: 2018/10/29 15:06
 * @Description: 分布式锁的简单实现代码
 */
public class DistributedLock {
    private final JedisPool jedisPool;

    public DistributedLock(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    /**
     * 加锁
     *
     * @param lockName        锁的key
     * @param acquiredTimeout 获取锁超时的超时时间
     * @param timeout         锁的超时时间
     * @return 锁标识
     */
    public String lockWithTimeout(String lockName, long acquiredTimeout, long timeout) {
        Jedis jedis = null;
        String retIdentifier = null;

        try {
            //获取连接
            jedis = jedisPool.getResource();
            //随机生成一个value, 在释放锁的时候进行判断
            String identifier = UUID.randomUUID().toString();
            //锁名，即key
            String lockKey = "lock:" + lockName;
            //超时时间，上锁后超过此时间则自动释放锁
            int lockExpire = (int) (timeout / 1000);
            // 获取锁的超时时间，超过这个时间则放弃获取锁
            long end = System.currentTimeMillis() + acquiredTimeout;
            //循环判断是否可以上锁
            while (System.currentTimeMillis() < end) {
                if (jedis.setnx(lockKey, identifier) == 1) {
                    jedis.expire(lockKey, lockExpire);
                    // 返回value值，用于释放锁时间确认
                    retIdentifier = identifier;
                    return retIdentifier;
                }

                // 返回-1代表key没有设置超时时间，为key设置一个超时时间
                if (jedis.ttl(lockKey) == -1) {
                    jedis.expire(lockKey, lockExpire);
                }

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
        } catch (JedisException e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }

        return retIdentifier;
    }


    /**
     * 释放锁
     *
     * @param lockName   锁的key
     * @param identifire 释放锁的标识
     * @return 是否释放成功
     */
    public boolean releaseLock(String lockName, String identifire) {
        Jedis jedis = null;
        String lockKey = "lock:" + lockName;
        boolean retFlag = false;

        try {
            jedis = jedisPool.getResource();
            //循环判断锁是否删除成功
            while (true) {
                jedis.watch(lockKey);
                if (identifire.equals(jedis.get(lockKey))) {
                    Transaction transaction = jedis.multi();
                    transaction.del(lockKey);
                    List<Object> results = transaction.exec();
                    if (results == null) {
                        continue;
                    }
                    retFlag = true;
                }
                jedis.unwatch();
                return retFlag;
            }
        } catch (JedisException  e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }

        return retFlag;
    }
}
