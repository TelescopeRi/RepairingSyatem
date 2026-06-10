package com.dorm.repair.controller;

import com.dorm.repair.dto.UpdateOrderStatusDTO;
import com.dorm.repair.entity.Evaluation;
import com.dorm.repair.entity.FaultType;
import com.dorm.repair.entity.RepairOrder;
import com.dorm.repair.entity.User;
import com.dorm.repair.mapper.FaultTypeMapper;
import com.dorm.repair.mapper.UserMapper;
import com.dorm.repair.service.EvaluationService;
import com.dorm.repair.service.RepairOrderService;
import com.dorm.repair.service.UserService;
import com.dorm.repair.utils.FileUploadUtils;
import com.dorm.repair.vo.EvaluationVO;
import com.dorm.repair.vo.RepairOrderVO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/repair")
public class RepairController {
    
    private Long getUserId(Authentication auth) {
        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails) {
            return Long.parseLong(((UserDetails) principal).getUsername());
        }
        return Long.parseLong(principal.toString());
    }
    
    @Resource
    private RepairOrderService repairOrderService;
    
    @Resource
    private EvaluationService evaluationService;
    
    @Resource
    private UserMapper userMapper;
    
    @Resource
    private FaultTypeMapper faultTypeMapper;
    
    @Resource
    private UserService userService;
    
    @Resource
    private FileUploadUtils fileUploadUtils;
    
    @GetMapping("/orders/pending")
    public List<RepairOrderVO> getPendingOrders(Authentication auth) {
        Long userId = getUserId(auth);
        List<RepairOrder> orders = repairOrderService.findPendingTreat(userId);
        return convertToVOList(orders);
    }
    
    @GetMapping("/orders")
    public List<RepairOrderVO> getMyOrders(Authentication auth) {
        Long userId = getUserId(auth);
        List<RepairOrder> orders = repairOrderService.findByRepairmanId(userId);
        return convertToVOList(orders);
    }
    
    @GetMapping("/orders/{id}")
    public RepairOrderVO getOrderDetail(Authentication auth, @PathVariable Long id) {
        Long userId = getUserId(auth);
        RepairOrder order = repairOrderService.getById(id);
        
        if (order == null || !userId.equals(order.getRepairmanId())) {
            throw new RuntimeException("无权查看此工单");
        }
        
        return convertToVO(order);
    }
    
    @PostMapping("/orders/{id}/start")
    public void startRepair(Authentication auth, @PathVariable Long id) {
        Long userId = getUserId(auth);
        RepairOrder order = repairOrderService.getById(id);
        
        if (order == null || !userId.equals(order.getRepairmanId())) {
            throw new RuntimeException("无权操作此工单");
        }
        
        repairOrderService.startRepair(id);
    }
    
    @PostMapping("/orders/{id}/complete")
    public void completeRepair(Authentication auth, @PathVariable Long id, @RequestBody UpdateOrderStatusDTO dto) {
        Long userId = getUserId(auth);
        RepairOrder order = repairOrderService.getById(id);
        
        if (order == null || !userId.equals(order.getRepairmanId())) {
            throw new RuntimeException("无权操作此工单");
        }
        
        repairOrderService.completeRepair(id, dto.getRemark(), dto.getImages());
    }
    
    @GetMapping("/orders/{id}/evaluation")
    public EvaluationVO getEvaluation(Authentication auth, @PathVariable Long id) {
        Long userId = getUserId(auth);
        RepairOrder order = repairOrderService.getById(id);
        
        if (order == null || !userId.equals(order.getRepairmanId())) {
            throw new RuntimeException("无权查看此工单");
        }
        
        Evaluation evaluation = evaluationService.findByOrderId(id);
        if (evaluation == null) {
            return null;
        }
        
        EvaluationVO vo = new EvaluationVO();
        vo.setId(evaluation.getId());
        vo.setOrderId(evaluation.getOrderId());
        vo.setRating(evaluation.getRating());
        vo.setComment(evaluation.getComment());
        vo.setCreateTime(evaluation.getCreateTime());
        return vo;
    }
    
    @PutMapping("/password")
    public void changePassword(Authentication auth, @RequestBody Map<String, String> requestBody) {
        Long userId = getUserId(auth);
        String oldPassword = requestBody.get("oldPassword");
        String newPassword = requestBody.get("newPassword");
        
        userService.changePassword(userId, oldPassword, newPassword);
    }
    
    private List<RepairOrderVO> convertToVOList(List<RepairOrder> orders) {
        return orders.stream().map(this::convertToVO).collect(Collectors.toList());
    }
    
    private RepairOrderVO convertToVO(RepairOrder order) {
        RepairOrderVO vo = new RepairOrderVO();
        vo.setId(order.getId());
        vo.setBuilding(order.getBuilding());
        vo.setDormNumber(order.getDormNumber());
        vo.setDescription(order.getDescription());
        vo.setUrgency(order.getUrgency());
        vo.setStatus(order.getStatus());
        vo.setStatusColor(getStatusColor(order.getStatus()));
        vo.setSubmitTime(order.getSubmitTime());
        vo.setAssignTime(order.getAssignTime());
        vo.setStartTime(order.getStartTime());
        vo.setCompleteTime(order.getCompleteTime());
        vo.setRemark(order.getRemark());
        
        // 处理报修图片
        if (order.getImages() != null && !order.getImages().trim().isEmpty()) {
            vo.setImages(Arrays.asList(order.getImages().split(",")));
        }
        
        // 处理维修完成图片
        if (order.getRepairImages() != null && !order.getRepairImages().trim().isEmpty()) {
            vo.setRepairImages(Arrays.asList(order.getRepairImages().split(",")));
        }
        
        FaultType faultType = faultTypeMapper.selectById(order.getFaultTypeId());
        vo.setFaultTypeName(faultType != null ? faultType.getName() : "未知");
        
        User student = userMapper.selectById(order.getStudentId());
        vo.setStudentName(student != null ? student.getRealName() : "");
        
        return vo;
    }
    
    private String getStatusColor(String status) {
        switch (status) {
            case "PENDING_ASSIGN": return "gray";
            case "PENDING_TREAT": return "blue";
            case "IN_PROGRESS": return "orange";
            case "PENDING_CONFIRM": return "purple";
            case "COMPLETED": return "green";
            case "CANCELLED": return "red";
            default: return "gray";
        }
    }
    
    @PostMapping("/upload")
    public Map<String, Object> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        Map<String, Object> result = new HashMap<>();
        if (file == null || file.isEmpty()) {
            result.put("code", 400);
            result.put("message", "请选择要上传的文件");
            return result;
        }
        
        String url = fileUploadUtils.upload(file);
        if (url != null) {
            result.put("code", 200);
            result.put("message", "上传成功");
            result.put("data", Map.of("url", url));
        } else {
            result.put("code", 500);
            result.put("message", "上传失败");
        }
        return result;
    }
}
