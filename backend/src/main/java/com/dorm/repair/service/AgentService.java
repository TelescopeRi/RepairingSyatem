package com.dorm.repair.service;

import com.dorm.repair.dto.AgentResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface AgentService {
    
    AgentResponseDTO chat(String message);
    
    AgentResponseDTO parseFile(MultipartFile file);
    
    AgentResponseDTO batchImport(MultipartFile file, String type);
    
    AgentResponseDTO summarizeRepairStatus();
    
    AgentResponseDTO summarizeWorkerPerformance();
    
    AgentResponseDTO analyzeFaultTrend();
}