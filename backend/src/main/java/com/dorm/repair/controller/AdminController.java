package com.dorm.repair.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.repair.dto.AssignOrderDTO;
import com.dorm.repair.dto.BatchOperationResultDTO;
import com.dorm.repair.dto.RegisterDTO;
import com.dorm.repair.entity.*;
import com.dorm.repair.mapper.FaultTypeMapper;
import com.dorm.repair.mapper.UserMapper;
import com.dorm.repair.service.*;
import com.dorm.repair.utils.ExcelUtils;
import com.dorm.repair.utils.PasswordUtils;
import com.dorm.repair.vo.RepairOrderVO;
import com.dorm.repair.vo.StatisticsVO;
import com.dorm.repair.vo.UserVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.multipart.MultipartFile;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    
    @Resource
    private UserService userService;
    
    @Resource
    private RepairOrderService repairOrderService;
    
    @Resource
    private FaultTypeService faultTypeService;
    
    @Resource
    private BuildingService buildingService;
    
    @Resource
    private StatisticsService statisticsService;
    
    @Resource
    private EvaluationService evaluationService;
    
    @Resource
    private UserMapper userMapper;
    
    @Resource
    private FaultTypeMapper faultTypeMapper;
    
    @Resource
    private PasswordUtils passwordUtils;
    
    @GetMapping("/students")
    public List<UserVO> getStudents(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String building,
            @RequestParam(required = false) Integer status) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("role", "STUDENT");
        wrapper.eq("is_deleted", 0);
        
        if (name != null && !name.isEmpty()) {
            wrapper.like("real_name", name);
        }
        
        if (username != null && !username.isEmpty()) {
            wrapper.like("username", username);
        }
        
        if (building != null && !building.isEmpty()) {
            wrapper.eq("building", building);
        }
        
        if (status != null) {
            wrapper.eq("status", status);
        }
        
        wrapper.orderByDesc("create_time");
        
        List<User> users = userService.list(wrapper);
        return users.stream()
                .map(this::convertToUserVO)
                .collect(Collectors.toList());
    }
    
    @GetMapping("/repairmen")
    public List<UserVO> getRepairmen(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long specialtyId) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("role", "REPAIR");
        wrapper.eq("is_deleted", 0);
        
        if (name != null && !name.isEmpty()) {
            wrapper.like("real_name", name);
        }
        
        if (username != null && !username.isEmpty()) {
            wrapper.like("username", username);
        }
        
        if (status != null) {
            wrapper.eq("status", status);
        }
        
        if (specialtyId != null) {
            wrapper.like("specialty_ids", String.valueOf(specialtyId));
        }
        
        wrapper.orderByDesc("create_time");
        
        List<User> users = userService.list(wrapper);
        return users.stream()
                .map(this::convertToUserVO)
                .collect(Collectors.toList());
    }
    
    @PostMapping("/students")
    public UserVO createStudent(@RequestBody User user) {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername(user.getUsername());
        dto.setPassword("123456");
        dto.setRealName(user.getRealName());
        dto.setPhone(user.getPhone());
        dto.setRole("STUDENT");
        dto.setBuilding(user.getBuilding());
        dto.setDormNumber(user.getDormNumber());
        
        User created = userService.register(dto);
        return convertToUserVO(created);
    }
    
    @PostMapping("/repairmen")
    public UserVO createRepairman(@RequestBody User user) {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername(null);  // 让register方法自动生成工号
        dto.setPassword("123456");
        dto.setRealName(user.getRealName());
        dto.setPhone(user.getPhone());
        dto.setRole("REPAIR");
        dto.setBuilding(user.getBuilding());
        dto.setDormNumber(user.getDormNumber());
        
        User created = userService.register(dto);
        return convertToUserVO(created);
    }
    
    @PutMapping("/users/{id}")
    public UserVO updateUser(@PathVariable Long id, @RequestBody User user) {
        User existing = userService.getById(id);
        if (existing == null) {
            throw new RuntimeException("用户不存在");
        }
        existing.setRealName(user.getRealName());
        existing.setPhone(user.getPhone());
        existing.setBuilding(user.getBuilding());
        existing.setDormNumber(user.getDormNumber());
        existing.setSpecialtyIds(user.getSpecialtyIds());
        userService.updateById(existing);
        return convertToUserVO(existing);
    }
    
    @PutMapping("/users/{id}/status")
    public void updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.updateStatus(id, status);
    }
    
    @PutMapping("/users/{id}/password")
    public void resetPassword(@PathVariable Long id, @RequestParam String password) {
        userService.resetPassword(id, password);
    }
    
    @DeleteMapping("/users/{id}")
    public void deleteUser(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 检查是否有关联工单
        QueryWrapper<RepairOrder> wrapper = new QueryWrapper<>();
        wrapper.eq("student_id", id).or().eq("repairman_id", id);
        long count = repairOrderService.count(wrapper);
        if (count > 0) {
            throw new RuntimeException("该用户有 " + count + " 个关联工单，无法删除。请先处理相关工单或禁用该用户。");
        }
        
        // 假删除：设置is_deleted为1
        user.setIsDeleted(1);
        userMapper.updateById(user);
    }
    
    @GetMapping("/orders")
    public List<RepairOrderVO> getOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String building,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        QueryWrapper<RepairOrder> wrapper = new QueryWrapper<>();
        
        if (status != null && !status.isEmpty()) {
            wrapper.eq("status", status);
        }
        
        if (building != null && !building.isEmpty()) {
            wrapper.eq("building", building);
        }
        
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge("submit_time", startDate + " 00:00:00");
        }
        
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le("submit_time", endDate + " 23:59:59");
        }
        
        wrapper.orderByDesc("submit_time");
        
        List<RepairOrder> orders = repairOrderService.list(wrapper);
        return convertToVOList(orders);
    }
    
    @GetMapping("/orders/{id}")
    public RepairOrderVO getOrderDetail(@PathVariable Long id) {
        RepairOrder order = repairOrderService.getById(id);
        if (order == null) {
            throw new RuntimeException("工单不存在");
        }
        return convertToVO(order);
    }
    
    @PostMapping("/orders/{id}/assign")
    public void assignOrder(@PathVariable Long id, @RequestBody AssignOrderDTO dto) {
        repairOrderService.assignOrder(id, dto.getRepairmanId());
    }

    @GetMapping("/fault-types")
    public List<FaultType> getFaultTypes() {
        return faultTypeService.list();
    }
    
    @PostMapping("/fault-types")
    public FaultType createFaultType(@RequestBody FaultType faultType) {
        faultTypeService.save(faultType);
        return faultType;
    }
    
    @PutMapping("/fault-types/{id}")
    public FaultType updateFaultType(@PathVariable Long id, @RequestBody FaultType faultType) {
        FaultType existing = faultTypeService.getById(id);
        if (existing == null) {
            throw new RuntimeException("故障类型不存在");
        }
        existing.setName(faultType.getName());
        existing.setSortOrder(faultType.getSortOrder());
        existing.setStatus(faultType.getStatus());
        faultTypeService.updateById(existing);
        return existing;
    }
    
    @DeleteMapping("/fault-types/{id}")
    public void deleteFaultType(@PathVariable Long id) {
        FaultType faultType = faultTypeService.getById(id);
        if (faultType == null) {
            throw new RuntimeException("故障类型不存在");
        }
        
        // 检查是否有关联工单
        QueryWrapper<RepairOrder> wrapper = new QueryWrapper<>();
        wrapper.eq("fault_type_id", id);
        long count = repairOrderService.count(wrapper);
        if (count > 0) {
            throw new RuntimeException("该故障类型有 " + count + " 个关联工单，无法删除。请先处理相关工单或禁用该类型。");
        }
        
        // 假删除：设置is_deleted为1
        faultType.setIsDeleted(1);
        faultTypeService.updateById(faultType);
    }
    
    @GetMapping("/buildings")
    public List<Building> getBuildings() {
        return buildingService.list();
    }
    
    @PostMapping("/buildings")
    public Building createBuilding(@RequestBody Building building) {
        buildingService.save(building);
        return building;
    }
    
    @PutMapping("/buildings/{id}")
    public Building updateBuilding(@PathVariable Long id, @RequestBody Building building) {
        Building existing = buildingService.getById(id);
        if (existing == null) {
            throw new RuntimeException("楼栋不存在");
        }
        existing.setName(building.getName());
        existing.setStatus(building.getStatus());
        buildingService.updateById(existing);
        return existing;
    }
    
    @DeleteMapping("/buildings/{id}")
    public void deleteBuilding(@PathVariable Long id) {
        Building building = buildingService.getById(id);
        if (building == null) {
            throw new RuntimeException("楼栋不存在");
        }
        
        // 检查是否有关联工单
        QueryWrapper<RepairOrder> wrapper = new QueryWrapper<>();
        wrapper.eq("building", building.getName());
        long count = repairOrderService.count(wrapper);
        if (count > 0) {
            throw new RuntimeException("该楼栋有 " + count + " 个关联工单，无法删除。");
        }
        
        // 假删除：设置is_deleted为1
        building.setIsDeleted(1);
        buildingService.updateById(building);
    }
    
    @GetMapping("/statistics")
    public StatisticsVO getStatistics() {
        return statisticsService.getStatistics();
    }
    
    @GetMapping("/orders/export")
    public ResponseEntity<byte[]> exportOrders() throws IOException {
        List<RepairOrder> orders = repairOrderService.list();
        
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("工单列表");
        
        Row headerRow = sheet.createRow(0);
        String[] headers = {"工单ID", "学生姓名", "楼栋", "宿舍号", "故障类型", "紧急程度", "状态", "提交时间", "完成时间"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }
        
        int rowNum = 1;
        for (RepairOrder order : orders) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(order.getId());
            
            User student = userMapper.selectById(order.getStudentId());
            row.createCell(1).setCellValue(student != null ? student.getRealName() : "");
            
            row.createCell(2).setCellValue(order.getBuilding());
            row.createCell(3).setCellValue(order.getDormNumber());
            
            FaultType faultType = faultTypeMapper.selectById(order.getFaultTypeId());
            row.createCell(4).setCellValue(faultType != null ? faultType.getName() : "");
            
            row.createCell(5).setCellValue(order.getUrgency());
            row.createCell(6).setCellValue(order.getStatus());
            row.createCell(7).setCellValue(order.getSubmitTime() != null ? order.getSubmitTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
            row.createCell(8).setCellValue(order.getCompleteTime() != null ? order.getCompleteTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
        }
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        httpHeaders.setContentDispositionFormData("attachment", "orders.xlsx");
        
        return ResponseEntity.ok()
                .headers(httpHeaders)
                .body(outputStream.toByteArray());
    }
    
    @GetMapping("/students/export")
    public ResponseEntity<byte[]> exportStudents() throws IOException {
        List<User> students = userService.findByRole("STUDENT");
        
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("学生列表");
        
        Row headerRow = sheet.createRow(0);
        String[] headers = {"学号", "姓名", "电话号码", "楼栋", "宿舍号", "状态"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }
        
        int rowNum = 1;
        for (User student : students) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(student.getUsername());
            row.createCell(1).setCellValue(student.getRealName());
            row.createCell(2).setCellValue(student.getPhone() != null ? student.getPhone() : "");
            row.createCell(3).setCellValue(student.getBuilding() != null ? student.getBuilding() : "");
            row.createCell(4).setCellValue(student.getDormNumber() != null ? student.getDormNumber() : "");
            row.createCell(5).setCellValue(student.getStatus() == 1 ? "启用" : "禁用");
        }
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        httpHeaders.setContentDispositionFormData("attachment", "students.xlsx");
        
        return ResponseEntity.ok()
                .headers(httpHeaders)
                .body(outputStream.toByteArray());
    }

    private UserVO convertToUserVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setPhone(user.getPhone());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        vo.setBuilding(user.getBuilding());
        vo.setDormNumber(user.getDormNumber());
        vo.setSpecialtyIds(user.getSpecialtyIds());
        // 将specialtyIds转换为名称
        if (user.getSpecialtyIds() != null && !user.getSpecialtyIds().isEmpty()) {
            String[] ids = user.getSpecialtyIds().split(",");
            StringBuilder names = new StringBuilder();
            for (String id : ids) {
                FaultType faultType = faultTypeMapper.selectById(Long.parseLong(id.trim()));
                if (faultType != null) {
                    if (names.length() > 0) names.append(", ");
                    names.append(faultType.getName());
                }
            }
            vo.setSpecialtyNames(names.toString());
        }
        return vo;
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
        vo.setSubmitTime(order.getSubmitTime());
        vo.setAssignTime(order.getAssignTime());
        vo.setStartTime(order.getStartTime());
        vo.setCompleteTime(order.getCompleteTime());
        vo.setRemark(order.getRemark());
        
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
        
        if (order.getRepairmanId() != null) {
            User repairman = userMapper.selectById(order.getRepairmanId());
            vo.setRepairmanName(repairman != null ? repairman.getRealName() : "");
        }
        
        Evaluation evaluation = evaluationService.findByOrderId(order.getId());
        if (evaluation != null) {
            com.dorm.repair.vo.EvaluationVO evalVO = new com.dorm.repair.vo.EvaluationVO();
            evalVO.setId(evaluation.getId());
            evalVO.setOrderId(evaluation.getOrderId());
            evalVO.setRating(evaluation.getRating());
            evalVO.setComment(evaluation.getComment());
            evalVO.setCreateTime(evaluation.getCreateTime());
            vo.setEvaluation(evalVO);
        }
        
        return vo;
    }

    // 批量导入学生
    @PostMapping("/students/import")
    public BatchOperationResultDTO importStudents(@RequestParam("file") MultipartFile file) {
        BatchOperationResultDTO result = new BatchOperationResultDTO();
        
        if (file == null || file.isEmpty()) {
            result.addFail("请选择要上传的文件");
            return result;
        }
        
        try {
            List<User> users = ExcelUtils.parseStudentExcel(file);
            result.setTotalCount(users.size());
            
            for (User user : users) {
                try {
                    // 检查学号是否已存在
                    QueryWrapper<User> wrapper = new QueryWrapper<>();
                    wrapper.eq("username", user.getUsername());
                    if (userService.count(wrapper) > 0) {
                        result.addFail("学号 " + user.getUsername() + " 已存在");
                        continue;
                    }
                    
                    userService.save(user);
                    result.addSuccess("学号 " + user.getUsername() + " 导入成功");
                } catch (Exception e) {
                    result.addFail("学号 " + user.getUsername() + " 导入失败: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            result.addFail("文件解析失败: " + e.getMessage());
        }
        
        return result;
    }

    // 批量导入修理工
    @PostMapping("/repairmen/import")
    public BatchOperationResultDTO importRepairmen(@RequestParam("file") MultipartFile file) {
        BatchOperationResultDTO result = new BatchOperationResultDTO();
        
        if (file == null || file.isEmpty()) {
            result.addFail("请选择要上传的文件");
            return result;
        }
        
        try {
            List<User> users = ExcelUtils.parseRepairmanExcel(file);
            result.setTotalCount(users.size());
            
            for (User user : users) {
                try {
                    // 检查工号是否已存在
                    QueryWrapper<User> wrapper = new QueryWrapper<>();
                    wrapper.eq("username", user.getUsername());
                    if (userService.count(wrapper) > 0) {
                        result.addFail("工号 " + user.getUsername() + " 已存在");
                        continue;
                    }
                    
                    userService.save(user);
                    result.addSuccess("工号 " + user.getUsername() + " 导入成功");
                } catch (Exception e) {
                    result.addFail("工号 " + user.getUsername() + " 导入失败: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            result.addFail("文件解析失败: " + e.getMessage());
        }
        
        return result;
    }

    // 批量禁用学生
    @PostMapping("/students/disable")
    public BatchOperationResultDTO disableStudents(@RequestParam("file") MultipartFile file) {
        BatchOperationResultDTO result = new BatchOperationResultDTO();
        
        if (file == null || file.isEmpty()) {
            result.addFail("请选择要上传的文件");
            return result;
        }
        
        try {
            List<String> usernames = ExcelUtils.parseUsernameExcel(file);
            result.setTotalCount(usernames.size());
            
            for (String username : usernames) {
                try {
                    User user = userService.findByUsername(username);
                    if (user == null) {
                        result.addFail("学号 " + username + " 不存在");
                        continue;
                    }
                    
                    if (!"STUDENT".equals(user.getRole())) {
                        result.addFail("学号 " + username + " 不是学生用户");
                        continue;
                    }
                    
                    user.setStatus(0);
                    userService.updateById(user);
                    result.addSuccess("学号 " + username + " 已禁用");
                } catch (Exception e) {
                    result.addFail("学号 " + username + " 禁用失败: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            result.addFail("文件解析失败: " + e.getMessage());
        }
        
        return result;
    }

    // 批量启用学生
    @PostMapping("/students/enable")
    public BatchOperationResultDTO enableStudents(@RequestParam("file") MultipartFile file) {
        BatchOperationResultDTO result = new BatchOperationResultDTO();
        
        if (file == null || file.isEmpty()) {
            result.addFail("请选择要上传的文件");
            return result;
        }
        
        try {
            List<String> usernames = ExcelUtils.parseUsernameExcel(file);
            result.setTotalCount(usernames.size());
            
            for (String username : usernames) {
                try {
                    User user = userService.findByUsername(username);
                    if (user == null) {
                        result.addFail("学号 " + username + " 不存在");
                        continue;
                    }
                    
                    if (!"STUDENT".equals(user.getRole())) {
                        result.addFail("学号 " + username + " 不是学生用户");
                        continue;
                    }
                    
                    user.setStatus(1);
                    userService.updateById(user);
                    result.addSuccess("学号 " + username + " 已启用");
                } catch (Exception e) {
                    result.addFail("学号 " + username + " 启用失败: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            result.addFail("文件解析失败: " + e.getMessage());
        }
        
        return result;
    }
}
