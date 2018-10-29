package com.github.youngtwu.distributedlock.controller;

import com.github.youngtwu.distributedlock.service.DistributedLockService;

/**
 * @Auther: ASUS
 * @Date: 2018/10/29 16:50
 * @Description:
 */
public class SeckillThread extends Thread{

    private DistributedLockService distributedLockService;

    public SeckillThread(DistributedLockService distributedLockService) {
        this.distributedLockService = distributedLockService;
    }

    @Override
    public void run() {
        distributedLockService.seckill();
    }

    public static void main(String[] args) {
        DistributedLockService distributedLockService = new DistributedLockService();
        for (int i = 0; i < 50; i++) {
            SeckillThread seckillThread = new SeckillThread(distributedLockService);
            seckillThread.start();
        }
    }
}
