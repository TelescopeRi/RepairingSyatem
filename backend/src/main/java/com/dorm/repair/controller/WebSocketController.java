package com.dorm.repair.controller;

import com.dorm.repair.entity.RepairOrder;
import com.dorm.repair.service.RepairOrderService;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * WebSocket消息控制器
 * 用于实现实时消息推送
 */
@RestController
@RequestMapping("/api/ws")
public class WebSocketController {

    @Resource
    private SimpMessagingTemplate messagingTemplate;

    @Resource
    @Lazy
    private RepairOrderService repairOrderService;

    /**
     * 发送工单更新消息给所有客户端
     *
     * @param order 更新后的工单
     */
    public void sendOrderUpdate(RepairOrder order) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "ORDER_UPDATE");
        message.put("data", order);
        
        // 发送给管理员
        messagingTemplate.convertAndSend("/topic/admin/orders", message);
        // 发送给学生
        messagingTemplate.convertAndSend("/topic/student/orders", message);
        // 发送给修理工
        messagingTemplate.convertAndSend("/topic/repairman/orders", message);
    }

    /**
     * 发送新工单消息（主要通知管理员）
     *
     * @param order 新创建的工单
     */
    public void sendNewOrder(RepairOrder order) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "NEW_ORDER");
        message.put("data", order);
        
        // 发送给管理员（需要分配工单）
        messagingTemplate.convertAndSend("/topic/admin/orders", message);
        // 发送给创建工单的学生
        messagingTemplate.convertAndSend("/queue/student/" + order.getStudentId(), message);
    }

    /**
     * 发送工单状态变更消息（通知相关学生）
     *
     * @param orderId 工单ID
     * @param status  新状态
     */
    public void sendOrderStatusChange(Long orderId, String status) {
        RepairOrder order = repairOrderService.getById(orderId);
        if (order == null) {
            return;
        }
        
        Map<String, Object> message = new HashMap<>();
        message.put("type", "STATUS_CHANGE");
        message.put("orderId", orderId);
        message.put("status", status);
        message.put("data", order);
        
        // 发送给管理员
        messagingTemplate.convertAndSend("/topic/admin/orders", message);
        // 发送给修理工
        messagingTemplate.convertAndSend("/topic/repairman/orders", message);
        
        // 发送给相关学生（通过队列精确推送）
        messagingTemplate.convertAndSend("/queue/student/" + order.getStudentId(), message);
        
        // 如果有修理工，也发送给修理工队列
        if (order.getRepairmanId() != null) {
            messagingTemplate.convertAndSend("/queue/repairman/" + order.getRepairmanId(), message);
        }
    }

    /**
     * 发送分配工单消息给特定修理工和相关学生
     *
     * @param order     工单
     * @param repairmanId 修理工ID
     */
    public void sendOrderAssigned(RepairOrder order, Long repairmanId) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "ORDER_ASSIGNED");
        message.put("data", order);
        
        // 发送给指定修理工（精确推送）
        messagingTemplate.convertAndSend("/queue/repairman/" + repairmanId, message);
        // 发送给管理员
        messagingTemplate.convertAndSend("/topic/admin/orders", message);
        // 发送给工单所属学生（精确推送）
        messagingTemplate.convertAndSend("/queue/student/" + order.getStudentId(), message);
    }

    /**
     * 发送消息给特定学生
     *
     * @param studentId 学生ID
     * @param message   消息内容
     */
    public void sendToStudent(Long studentId, Map<String, Object> message) {
        messagingTemplate.convertAndSend("/queue/student/" + studentId, message);
    }

    /**
     * 发送消息给特定修理工
     *
     * @param repairmanId 修理工ID
     * @param message     消息内容
     */
    public void sendToRepairman(Long repairmanId, Map<String, Object> message) {
        messagingTemplate.convertAndSend("/queue/repairman/" + repairmanId, message);
    }

    /**
     * 广播消息给所有客户端
     *
     * @param message 消息内容
     */
    public void broadcast(String message) {
        messagingTemplate.convertAndSend("/topic/public", message);
    }

    /**
     * 客户端发送消息的端点（用于测试）
     */
    @MessageMapping("/hello")
    @SendTo("/topic/public")
    public Map<String, Object> greeting(Map<String, String> payload) throws Exception {
        Map<String, Object> response = new HashMap<>();
        response.put("type", "GREETING");
        response.put("message", "Hello, " + payload.get("name") + "!");
        return response;
    }

    /**
     * 获取待分配工单数量（用于管理员端显示）
     */
    @GetMapping("/pending-count")
    public Map<String, Object> getPendingCount() {
        List<RepairOrder> pendingOrders = repairOrderService.findPendingAssign();
        Map<String, Object> result = new HashMap<>();
        result.put("count", pendingOrders.size());
        return result;
    }
}