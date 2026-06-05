package com.dorm.repair.scheduler;

import com.dorm.repair.service.RepairOrderService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class OrderTimeoutScheduler {
    
    @Resource
    private RepairOrderService repairOrderService;
    
    @Scheduled(cron = "0 0 * * * ?")
    public void checkPendingConfirmTimeout() {
        repairOrderService.autoCompletePendingConfirm();
    }
}
