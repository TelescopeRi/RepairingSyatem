package com.dorm.repair.controller;

import com.dorm.repair.dto.EvaluationDTO;
import com.dorm.repair.dto.RepairOrderDTO;
import com.dorm.repair.entity.Evaluation;
import com.dorm.repair.entity.FaultType;
import com.dorm.repair.entity.RepairOrder;
import com.dorm.repair.entity.User;
import com.dorm.repair.mapper.BuildingMapper;
import com.dorm.repair.mapper.FaultTypeMapper;
import com.dorm.repair.mapper.UserMapper;
import com.dorm.repair.service.EvaluationService;
import com.dorm.repair.service.RepairOrderService;
import com.dorm.repair.service.UserService;
import com.dorm.repair.utils.FileUploadUtils;
import com.dorm.repair.vo.EvaluationVO;
import com.dorm.repair.vo.RepairOrderVO;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/student")
public class StudentController {
    
    @Resource
    private RepairOrderService repairOrderService;
    
    @Resource
    private EvaluationService evaluationService;
    
    @Resource
    private UserMapper userMapper;
    
    @Resource
    private FaultTypeMapper faultTypeMapper;
    
    @Resource
    private BuildingMapper buildingMapper;
    
    @Resource
    private FileUploadUtils fileUploadUtils;
    
    @GetMapping("/orders")
    public List<RepairOrderVO> getMyOrders(Authentication auth, @RequestParam(required = false) String status) {
        Long userId = Long.parseLong(auth.getPrincipal().toString());
        
        List<RepairOrder> orders;
        if (status != null && !status.isEmpty()) {
            orders = repairOrderService.findByStudentId(userId).stream()
                    .filter(o -> status.equals(o.getStatus()))
                    .collect(Collectors.toList());
        } else {
            orders = repairOrderService.findByStudentId(userId);
        }
        
        return convertToVOList(orders);
    }
    
    @GetMapping("/orders/{id}")
    public RepairOrderVO getOrderDetail(Authentication auth, @PathVariable Long id) {
        Long userId = Long.parseLong(auth.getPrincipal().toString());
        RepairOrder order = repairOrderService.getById(id);
        
        if (order == null || !order.getStudentId().equals(userId)) {
            throw new RuntimeException("无权查看此工单");
        }
        
        return convertToVO(order);
    }
    
    @PostMapping("/orders")
    public RepairOrderVO createOrder(Authentication auth, @RequestBody RepairOrderDTO dto) {
        Long userId = Long.parseLong(auth.getPrincipal().toString());
        RepairOrder order = repairOrderService.createOrder(userId, dto);
        return convertToVO(order);
    }
    
    @PostMapping("/orders/{id}/cancel")
    public void cancelOrder(Authentication auth, @PathVariable Long id, @RequestBody Map<String, String> requestBody) {
        String reason = requestBody.get("reason");
        Long userId = Long.parseLong(auth.getPrincipal().toString());
        RepairOrder order = repairOrderService.getById(id);
        
        if (order == null || !order.getStudentId().equals(userId)) {
            throw new RuntimeException("无权取消此工单");
        }
        
        repairOrderService.cancelOrder(id, reason);
    }
    
    @PostMapping("/orders/{id}/evaluate")
    public EvaluationVO evaluateOrder(Authentication auth, @PathVariable Long id, @RequestBody EvaluationDTO dto) {
        Long userId = Long.parseLong(auth.getPrincipal().toString());
        dto.setOrderId(id);
        Evaluation evaluation = evaluationService.createEvaluation(userId, dto);
        
        EvaluationVO vo = new EvaluationVO();
        vo.setId(evaluation.getId());
        vo.setOrderId(evaluation.getOrderId());
        vo.setRating(evaluation.getRating());
        vo.setComment(evaluation.getComment());
        vo.setCreateTime(evaluation.getCreateTime());
        return vo;
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
    
    @GetMapping("/fault-types")
    public List<FaultType> getFaultTypes() {
        return faultTypeMapper.selectAllEnabled();
    }
    
    @GetMapping("/buildings")
    public List<com.dorm.repair.entity.Building> getBuildings() {
        return buildingMapper.selectAllEnabled();
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
        
        if (order.getImages() != null) {
            vo.setImages(Arrays.asList(order.getImages().split(",")));
        }
        
        FaultType faultType = faultTypeMapper.selectById(order.getFaultTypeId());
        vo.setFaultTypeName(faultType != null ? faultType.getName() : "未知");
        
        User student = userMapper.selectById(order.getStudentId());
        vo.setStudentName(student != null ? student.getRealName() : "");
        
        if (order.getRepairmanId() != null) {
            User repairman = userMapper.selectById(order.getRepairmanId());
            vo.setRepairmanName(repairman != null ? repairman.getRealName() : "");
        }
        
        Evaluation evaluation = evaluationService.findByOrderId(order.getId());
        if (evaluation != null) {
            EvaluationVO evalVO = new EvaluationVO();
            evalVO.setId(evaluation.getId());
            evalVO.setOrderId(evaluation.getOrderId());
            evalVO.setRating(evaluation.getRating());
            evalVO.setComment(evaluation.getComment());
            evalVO.setCreateTime(evaluation.getCreateTime());
            vo.setEvaluation(evalVO);
        }
        
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
}
