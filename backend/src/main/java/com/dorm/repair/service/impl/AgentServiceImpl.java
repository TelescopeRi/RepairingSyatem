package com.dorm.repair.service.impl;

import com.dorm.repair.config.AgentConfig;
import com.dorm.repair.dto.AgentResponseDTO;
import com.dorm.repair.dto.RegisterDTO;
import com.dorm.repair.entity.FaultType;
import com.dorm.repair.entity.RepairOrder;
import com.dorm.repair.entity.User;
import com.dorm.repair.mapper.FaultTypeMapper;
import com.dorm.repair.service.AgentService;
import com.dorm.repair.service.RepairOrderService;
import com.dorm.repair.service.UserService;
import com.dorm.repair.utils.ExcelUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AgentServiceImpl implements AgentService {
    
    @Resource
    private AgentConfig agentConfig;
    
    @Resource
    private UserService userService;
    
    @Resource
    private RepairOrderService repairOrderService;
    
    @Resource
    private FaultTypeMapper faultTypeMapper;
    
    @Resource
    private ObjectMapper objectMapper;
    
    private static final String SYSTEM_PROMPT = 
        "你是一个宿舍维修管理系统的智能助手，负责帮助管理员管理学生和修理工信息，分析维修工单数据。\n" +
        "你的职责包括：\n" +
        "1. 回答管理员关于系统操作的问题\n" +
        "2. 分析报修情况和维修工单数据\n" +
        "3. 帮助总结维修工绩效\n" +
        "4. 提供数据导入的指导\n" +
        "5. 基于提供的实时数据回答具体问题\n" +
        "请用中文友好地回答问题，回答时要基于提供的真实数据，不要编造数据。";
    
    @Override
    public AgentResponseDTO chat(String message) {
        AgentResponseDTO response = new AgentResponseDTO();
        try {
            String prompt = buildPrompt(message);
            String result = callDeepSeekAPI(prompt);
            response.setContent(result);
            response.setType("chat");
            response.setSuccess(true);
        } catch (Exception e) {
            response.setSuccess(false);
            response.setError("调用大模型失败: " + e.getMessage());
        }
        return response;
    }
    
    @Override
    public AgentResponseDTO parseFile(MultipartFile file) {
        AgentResponseDTO response = new AgentResponseDTO();
        List<AgentResponseDTO.ParseResultItem> results = new ArrayList<>();
        
        try {
            String fileName = file.getOriginalFilename();
            if (fileName != null && fileName.toLowerCase().endsWith(".csv")) {
                results = parseCsvFile(file.getInputStream());
            } else {
                results = parseTxtFile(file.getInputStream());
            }
            
            response.setParseResults(results);
            response.setType("parse");
            response.setSuccess(true);
            response.setContent("文件解析完成，共 " + results.size() + " 条记录");
        } catch (Exception e) {
            response.setSuccess(false);
            response.setError("文件解析失败: " + e.getMessage());
        }
        return response;
    }
    
    @Override
    public AgentResponseDTO batchImport(MultipartFile file, String type) {
        AgentResponseDTO response = new AgentResponseDTO();
        int successCount = 0;
        int failCount = 0;
        List<String> errors = new ArrayList<>();
        
        try {
            String fileName = file.getOriginalFilename();
            List<User> users;
            
            // 根据文件类型选择解析方式
            if (fileName != null && (fileName.toLowerCase().endsWith(".xlsx") || fileName.toLowerCase().endsWith(".xls"))) {
                // 使用 ExcelUtils 解析 Excel 文件
                if ("student".equalsIgnoreCase(type)) {
                    users = ExcelUtils.parseStudentExcel(file);
                } else if ("repair".equalsIgnoreCase(type)) {
                    users = ExcelUtils.parseRepairmanExcel(file);
                    // 将故障名称转换为ID
                    convertSpecialtyNamesToIds(users);
                    // 批量导入时自动生成递增工号
                    generateRepairmanCodes(users);
                } else {
                    response.setSuccess(false);
                    response.setError("不支持的导入类型: " + type);
                    return response;
                }
                
                // 批量保存用户
                for (User user : users) {
                    try {
                        userService.save(user);
                        successCount++;
                    } catch (Exception e) {
                        failCount++;
                        errors.add("用户 " + user.getUsername() + ": " + e.getMessage());
                    }
                }
            } else {
                // 解析 CSV/TXT 文件
                List<AgentResponseDTO.ParseResultItem> items;
                if (fileName != null && fileName.toLowerCase().endsWith(".csv")) {
                    items = parseCsvFile(file.getInputStream());
                } else {
                    items = parseTxtFile(file.getInputStream());
                }
                
                for (AgentResponseDTO.ParseResultItem item : items) {
                    try {
                        RegisterDTO dto = new RegisterDTO();
                        dto.setRealName(item.getName());
                        // 修理工导入时自动生成工号，不使用文件中的工号
                        if (type.equalsIgnoreCase("repair")) {
                            dto.setUsername(null);  // 让register方法自动生成工号
                        } else {
                            dto.setUsername(item.getUsername());
                        }
                        dto.setPassword("123456"); // 默认密码
                        dto.setPhone(item.getPhone());
                        dto.setRole(type.equalsIgnoreCase("repair") ? "REPAIR" : "STUDENT");
                        dto.setBuilding(item.getBuilding());
                        dto.setDormNumber(item.getDormNumber());
                        
                        userService.register(dto);
                        successCount++;
                    } catch (Exception e) {
                        failCount++;
                        errors.add("第" + item.getRowIndex() + "行: " + e.getMessage());
                    }
                }
            }
            
            response.setSuccess(true);
            response.setType("import");
            response.setContent(String.format("批量导入完成！成功: %d 条，失败: %d 条", successCount, failCount));
            if (!errors.isEmpty()) {
                response.setContent(response.getContent() + "\n失败详情:\n" + String.join("\n", errors));
            }
        } catch (Exception e) {
            response.setSuccess(false);
            response.setError("批量导入失败: " + e.getMessage());
        }
        return response;
    }
    
    @Override
    public AgentResponseDTO summarizeRepairStatus() {
        AgentResponseDTO response = new AgentResponseDTO();
        try {
            List<RepairOrder> orders = repairOrderService.list();
            
            long todayCount = orders.stream()
                .filter(o -> o.getSubmitTime() != null && o.getSubmitTime().toLocalDate().equals(LocalDate.now()))
                .count();
            
            long pendingCount = orders.stream()
                .filter(o -> "PENDING_ASSIGN".equals(o.getStatus()))
                .count();
            
            long processingCount = orders.stream()
                .filter(o -> "PROCESSING".equals(o.getStatus()))
                .count();
            
            long completedCount = orders.stream()
                .filter(o -> "COMPLETED".equals(o.getStatus()))
                .count();
            
            Map<String, Long> faultTypeCount = orders.stream()
                .filter(o -> o.getFaultTypeId() != null)
                .collect(Collectors.groupingBy(
                    o -> String.valueOf(o.getFaultTypeId()), 
                    Collectors.counting()
                ));
            
            Map<String, Long> buildingCount = orders.stream()
                .filter(o -> o.getBuilding() != null)
                .collect(Collectors.groupingBy(
                    o -> o.getBuilding(), 
                    Collectors.counting()
                ));
            
            String summary = String.format(
                "📊 报修情况汇总\n\n" +
                "今日新增报修: %d 单\n" +
                "待分配工单: %d 单\n" +
                "处理中工单: %d 单\n" +
                "已完成工单: %d 单\n\n" +
                "🏢 各楼栋报修分布:\n%s\n\n" +
                "🔧 故障类型统计:\n%s",
                todayCount, pendingCount, processingCount, completedCount,
                buildingCount.entrySet().stream()
                    .map(e -> "  - " + e.getKey() + ": " + e.getValue() + " 单")
                    .collect(Collectors.joining("\n")),
                faultTypeCount.entrySet().stream()
                    .map(e -> "  - 类型" + e.getKey() + ": " + e.getValue() + " 单")
                    .collect(Collectors.joining("\n"))
            );
            
            response.setContent(summary);
            response.setType("summary");
            response.setSuccess(true);
        } catch (Exception e) {
            response.setSuccess(false);
            response.setError("统计失败: " + e.getMessage());
        }
        return response;
    }
    
    @Override
    public AgentResponseDTO summarizeWorkerPerformance() {
        AgentResponseDTO response = new AgentResponseDTO();
        try {
            List<User> repairmen = userService.findByRole("REPAIR");
            List<RepairOrder> orders = repairOrderService.list();
            
            StringBuilder summary = new StringBuilder();
            summary.append("👷 维修工绩效分析\n\n");
            
            for (User repairman : repairmen) {
                long completedOrders = orders.stream()
                    .filter(o -> repairman.getId().equals(o.getRepairmanId()))
                    .filter(o -> "COMPLETED".equals(o.getStatus()))
                    .count();
                
                long processingOrders = orders.stream()
                    .filter(o -> repairman.getId().equals(o.getRepairmanId()))
                    .filter(o -> "PROCESSING".equals(o.getStatus()))
                    .count();
                
                summary.append(String.format(
                    "**%s**\n" +
                    "  - 已完成工单: %d 单\n" +
                    "  - 处理中工单: %d 单\n",
                    repairman.getRealName(), completedOrders, processingOrders
                ));
            }
            
            response.setContent(summary.toString());
            response.setType("summary");
            response.setSuccess(true);
        } catch (Exception e) {
            response.setSuccess(false);
            response.setError("绩效分析失败: " + e.getMessage());
        }
        return response;
    }
    
    @Override
    public AgentResponseDTO analyzeFaultTrend() {
        AgentResponseDTO response = new AgentResponseDTO();
        try {
            List<RepairOrder> orders = repairOrderService.list();
            
            Map<LocalDate, Long> dailyCount = orders.stream()
                .filter(o -> o.getSubmitTime() != null)
                .collect(Collectors.groupingBy(
                    o -> o.getSubmitTime().toLocalDate(),
                    Collectors.counting()
                ));
            
            StringBuilder trend = new StringBuilder();
            trend.append("📈 故障趋势分析\n\n");
            trend.append("近7天报修趋势:\n");
            
            LocalDate today = LocalDate.now();
            for (int i = 6; i >= 0; i--) {
                LocalDate date = today.minusDays(i);
                long count = dailyCount.getOrDefault(date, 0L);
                trend.append(String.format(
                    "  %s: %d 单\n",
                    date.format(DateTimeFormatter.ofPattern("MM-dd")),
                    count
                ));
            }
            
            response.setContent(trend.toString());
            response.setType("summary");
            response.setSuccess(true);
        } catch (Exception e) {
            response.setSuccess(false);
            response.setError("趋势分析失败: " + e.getMessage());
        }
        return response;
    }
    
    private String buildPrompt(String userMessage) {
        // 获取实时数据库数据作为上下文
        StringBuilder context = new StringBuilder();
        context.append("\n\n【当前系统实时数据】\n");
        
        try {
            // 判断用户是否询问特定日期的数据
            LocalDate queryDate = extractDateFromQuery(userMessage);
            
            // 工单统计
            List<RepairOrder> allOrders = repairOrderService.list();
            List<RepairOrder> orders = allOrders;
            
            // 如果用户询问特定日期，过滤该日期的工单
            if (queryDate != null) {
                orders = allOrders.stream()
                    .filter(o -> o.getSubmitTime() != null 
                        && o.getSubmitTime().toLocalDate().equals(queryDate))
                    .collect(Collectors.toList());
                context.append(String.format("【数据范围】%s的工单数据\n\n", queryDate.toString()));
            } else {
                context.append("【数据范围】所有历史工单数据\n\n");
            }
            
            long totalOrders = orders.size();
            
            long todayCount = orders.stream()
                .filter(o -> o.getSubmitTime() != null 
                    && o.getSubmitTime().toLocalDate().equals(LocalDate.now()))
                .count();
            
            long pendingAssignCount = orders.stream()
                .filter(o -> "PENDING_ASSIGN".equals(o.getStatus()))
                .count();
            
            long pendingTreatCount = orders.stream()
                .filter(o -> "PENDING_TREAT".equals(o.getStatus()))
                .count();
            
            long inProgressCount = orders.stream()
                .filter(o -> "IN_PROGRESS".equals(o.getStatus()))
                .count();
            
            long pendingConfirmCount = orders.stream()
                .filter(o -> "PENDING_CONFIRM".equals(o.getStatus()))
                .count();
            
            long completedCount = orders.stream()
                .filter(o -> "COMPLETED".equals(o.getStatus()))
                .count();
            
            long cancelledCount = orders.stream()
                .filter(o -> "CANCELLED".equals(o.getStatus()))
                .count();
            
            context.append("=== 工单统计 ===\n");
            context.append(String.format("总工单数: %d\n", totalOrders));
            if (queryDate == null) {
                context.append(String.format("今日新增: %d\n", todayCount));
            }
            context.append(String.format("待分配(PENDING_ASSIGN): %d\n", pendingAssignCount));
            context.append(String.format("待处理(PENDING_TREAT): %d\n", pendingTreatCount));
            context.append(String.format("进行中(IN_PROGRESS): %d\n", inProgressCount));
            context.append(String.format("待确认(PENDING_CONFIRM): %d\n", pendingConfirmCount));
            context.append(String.format("已完成(COMPLETED): %d\n", completedCount));
            context.append(String.format("已取消(CANCELLED): %d\n", cancelledCount));
            
            // 楼栋分布
            Map<String, Long> buildingCount = orders.stream()
                .filter(o -> o.getBuilding() != null)
                .collect(Collectors.groupingBy(
                    RepairOrder::getBuilding, 
                    Collectors.counting()
                ));
            
            context.append("\n=== 楼栋报修分布 ===\n");
            buildingCount.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .forEach(e -> context.append(String.format("%s: %d单\n", e.getKey(), e.getValue())));
            
            // 故障类型分布
            Map<Long, Long> faultTypeCount = orders.stream()
                .filter(o -> o.getFaultTypeId() != null)
                .collect(Collectors.groupingBy(
                    RepairOrder::getFaultTypeId, 
                    Collectors.counting()
                ));
            
            context.append("\n=== 故障类型分布 ===\n");
            Map<Long, String> faultTypeNames = new HashMap<>();
            faultTypeNames.put(1L, "水电维修");
            faultTypeNames.put(2L, "家具维修");
            faultTypeNames.put(3L, "电器维修");
            faultTypeNames.put(4L, "网络维修");
            faultTypeNames.put(5L, "门锁维修");
            faultTypeNames.put(6L, "墙面维修");
            faultTypeNames.put(7L, "其他");
            
            faultTypeCount.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .forEach(e -> {
                    String typeName = faultTypeNames.getOrDefault(e.getKey(), "类型" + e.getKey());
                    context.append(String.format("%s: %d单\n", typeName, e.getValue()));
                });
            
            // 维修工统计
            List<User> repairmen = userService.findByRole("REPAIR");
            context.append("\n=== 维修工信息 ===\n");
            context.append(String.format("维修工总数: %d\n", repairmen.size()));
            
            for (User repairman : repairmen) {
                long completed = orders.stream()
                    .filter(o -> repairman.getId().equals(o.getRepairmanId()))
                    .filter(o -> "COMPLETED".equals(o.getStatus()))
                    .count();
                
                long processing = orders.stream()
                    .filter(o -> repairman.getId().equals(o.getRepairmanId()))
                    .filter(o -> "IN_PROGRESS".equals(o.getStatus()) || "PENDING_TREAT".equals(o.getStatus()))
                    .count();
                
                String status = repairman.getStatus() == 1 ? "在线" : "离线";
                context.append(String.format("%s(ID:%d): 状态[%s], 已完成%d单, 处理中%d单\n", 
                    repairman.getRealName(), repairman.getId(), status, completed, processing));
            }
            
            // 学生统计
            List<User> students = userService.findByRole("STUDENT");
            context.append("\n=== 学生信息 ===\n");
            context.append(String.format("学生用户总数: %d\n", students.size()));
            
            // 近7天趋势（仅在查询全局数据时显示）
            if (queryDate == null) {
                context.append("\n=== 近7天报修趋势 ===\n");
                Map<LocalDate, Long> dailyCount = allOrders.stream()
                    .filter(o -> o.getSubmitTime() != null)
                    .collect(Collectors.groupingBy(
                        o -> o.getSubmitTime().toLocalDate(),
                        Collectors.counting()
                    ));
                
                LocalDate today = LocalDate.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
                for (int i = 6; i >= 0; i--) {
                    LocalDate date = today.minusDays(i);
                    long count = dailyCount.getOrDefault(date, 0L);
                    context.append(String.format("%s: %d单\n", date.format(formatter), count));
                }
            }
            
            // 紧急工单
            long urgentCount = orders.stream()
                .filter(o -> "URGENT".equals(o.getUrgency()))
                .filter(o -> !"COMPLETED".equals(o.getStatus()) && !"CANCELLED".equals(o.getStatus()))
                .count();
            
            context.append("\n=== 紧急工单 ===\n");
            context.append(String.format("待处理紧急工单: %d单\n", urgentCount));
            
            // 当前时间
            context.append("\n=== 当前时间 ===\n");
            context.append(String.format("日期: %s\n", LocalDate.now().toString()));
            context.append(String.format("时间: %s\n", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))));
            
        } catch (Exception e) {
            context.append("\n[数据获取异常: ").append(e.getMessage()).append("]\n");
        }
        
        return SYSTEM_PROMPT + context.toString() + "\n\n用户问题: " + userMessage;
    }
    
    /**
     * 从用户查询中提取日期
     */
    private LocalDate extractDateFromQuery(String query) {
        if (query == null || query.isEmpty()) {
            return null;
        }
        
        // 匹配日期格式：2026-06-04, 2026年6月4日, 6月4日, 6.4等
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            "(\\d{4})[-年](\\d{1,2})[-月](\\d{1,2})[日]?|" +  // 2026-06-04 或 2026年6月4日
            "(\\d{1,2})[月.](\\d{1,2})[日]?"  // 6月4日 或 6.4
        );
        
        java.util.regex.Matcher matcher = pattern.matcher(query);
        if (matcher.find()) {
            try {
                if (matcher.group(1) != null) {
                    // 完整日期格式
                    int year = Integer.parseInt(matcher.group(1));
                    int month = Integer.parseInt(matcher.group(2));
                    int day = Integer.parseInt(matcher.group(3));
                    return LocalDate.of(year, month, day);
                } else {
                    // 简写格式，假设是当前年份
                    int month = Integer.parseInt(matcher.group(4));
                    int day = Integer.parseInt(matcher.group(5));
                    return LocalDate.of(LocalDate.now().getYear(), month, day);
                }
            } catch (Exception e) {
                return null;
            }
        }
        
        return null;
    }
    
    private String callDeepSeekAPI(String prompt) throws Exception {
        URL url = new URL(agentConfig.getBaseUrl() + "/chat/completions");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + agentConfig.getApiKey());
        conn.setDoOutput(true);
        conn.setConnectTimeout(agentConfig.getTimeout());
        conn.setReadTimeout(agentConfig.getTimeout());
        
        Map<String, Object> body = new HashMap<>();
        body.put("model", agentConfig.getModel());
        body.put("temperature", agentConfig.getTemperature());
        
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", prompt);
        messages.add(userMsg);
        body.put("messages", messages);
        
        String jsonBody = objectMapper.writeValueAsString(body);
        conn.getOutputStream().write(jsonBody.getBytes(StandardCharsets.UTF_8));
        
        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                JsonNode root = objectMapper.readTree(reader);
                return root.get("choices").get(0).get("message").get("content").asText();
            }
        } else {
            throw new RuntimeException("API调用失败，状态码: " + responseCode);
        }
    }
    
    private List<AgentResponseDTO.ParseResultItem> parseCsvFile(InputStream inputStream) throws Exception {
        List<AgentResponseDTO.ParseResultItem> results = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            int rowIndex = 0;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                
                rowIndex++;
                AgentResponseDTO.ParseResultItem item = new AgentResponseDTO.ParseResultItem();
                item.setRowIndex(rowIndex);
                
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    // 格式：学号/工号, 姓名, 手机号, 楼栋, 宿舍号
                    item.setUsername(parts[0].trim());  // 第一列是学号/工号
                    item.setName(parts[1].trim());      // 第二列是姓名
                    if (parts.length > 2) item.setPhone(parts[2].trim());
                    if (parts.length > 3) item.setBuilding(parts[3].trim());
                    if (parts.length > 4) item.setDormNumber(parts[4].trim());
                    item.setStatus("valid");
                } else {
                    item.setStatus("error");
                    item.setErrorMessage("数据格式不正确");
                }
                results.add(item);
            }
        }
        return results;
    }
    
    private List<AgentResponseDTO.ParseResultItem> parseTxtFile(InputStream inputStream) throws Exception {
        List<AgentResponseDTO.ParseResultItem> results = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            int rowIndex = 0;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                // 跳过表头
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                
                rowIndex++;
                AgentResponseDTO.ParseResultItem item = new AgentResponseDTO.ParseResultItem();
                item.setRowIndex(rowIndex);
                
                String[] parts = line.split("\\s+");
                if (parts.length >= 2) {
                    // 格式：学号/工号 姓名 手机号 楼栋 宿舍号
                    item.setUsername(parts[0].trim());  // 第一列是学号/工号
                    item.setName(parts[1].trim());      // 第二列是姓名
                    if (parts.length > 2) item.setPhone(parts[2].trim());
                    if (parts.length > 3) item.setBuilding(parts[3].trim());
                    if (parts.length > 4) item.setDormNumber(parts[4].trim());
                    item.setStatus("valid");
                } else {
                    item.setStatus("error");
                    item.setErrorMessage("数据格式不正确");
                }
                results.add(item);
            }
        }
        return results;
    }
    
    /**
     * 将修理工的专业技能名称转换为故障类型ID（支持模糊匹配）
     */
    private void convertSpecialtyNamesToIds(List<User> users) {
        // 获取所有故障类型
        List<FaultType> faultTypes = faultTypeMapper.selectAllEnabled();
        
        // 创建名称到ID的映射（完全匹配）
        Map<String, Long> exactNameToIdMap = new HashMap<>();
        // 创建简短名称到ID的映射（模糊匹配，提取括号前的部分）
        Map<String, Long> shortNameToIdMap = new HashMap<>();
        
        for (FaultType faultType : faultTypes) {
            String name = faultType.getName();
            exactNameToIdMap.put(name, faultType.getId());
            
            // 提取括号前的简短名称
            String shortName = extractShortName(name);
            if (shortName != null && !shortName.equals(name)) {
                shortNameToIdMap.put(shortName, faultType.getId());
            }
        }
        
        // 转换每个用户的specialtyIds
        for (User user : users) {
            String specialtyNames = user.getSpecialtyIds();
            if (specialtyNames != null && !specialtyNames.isEmpty()) {
                String[] names = specialtyNames.split(",");
                List<String> ids = new ArrayList<>();
                for (String name : names) {
                    String trimmedName = name.trim();
                    // 先尝试完全匹配
                    Long id = exactNameToIdMap.get(trimmedName);
                    if (id == null) {
                        // 再尝试模糊匹配（简短名称）
                        id = shortNameToIdMap.get(trimmedName);
                    }
                    if (id != null) {
                        ids.add(String.valueOf(id));
                    }
                }
                user.setSpecialtyIds(ids.isEmpty() ? "" : String.join(",", ids));
            }
        }
    }
    
    /**
     * 提取故障名称的简短部分（括号前的内容）
     * 例如："水路故障（水管、水龙头、马桶）" -> "水路故障"
     */
    private String extractShortName(String fullName) {
        if (fullName == null || fullName.isEmpty()) {
            return null;
        }
        int bracketIndex = fullName.indexOf("（");
        if (bracketIndex == -1) {
            bracketIndex = fullName.indexOf("(");
        }
        if (bracketIndex > 0) {
            return fullName.substring(0, bracketIndex).trim();
        }
        return fullName;
    }
    
    /**
     * 批量生成修理工工号（4位数字，从0001递增）
     */
    private void generateRepairmanCodes(List<User> users) {
        // 查询数据库中所有修理工工号（4位纯数字格式）
        List<User> existingRepairmen = userService.findByRole("REPAIR");
        
        int maxSequence = 0;
        for (User user : existingRepairmen) {
            String username = user.getUsername();
            // 检查是否为4位纯数字格式
            if (username != null && username.matches("\\d{4}")) {
                try {
                    int sequence = Integer.parseInt(username);
                    if (sequence > maxSequence) {
                        maxSequence = sequence;
                    }
                } catch (NumberFormatException e) {
                    // 忽略无法解析的工号
                }
            }
        }
        
        // 为每个新用户生成递增工号
        for (User user : users) {
            maxSequence++;
            user.setUsername(String.format("%04d", maxSequence));
            // 默认密码为工号本身
            user.setPassword(com.dorm.repair.utils.PasswordUtils.encode(String.format("%04d", maxSequence)));
        }
    }
}