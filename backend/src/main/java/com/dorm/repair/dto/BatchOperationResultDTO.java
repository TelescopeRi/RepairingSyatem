package com.dorm.repair.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchOperationResultDTO {

    private int totalCount;
    
    private int successCount;
    
    private int failCount;
    
    private List<String> successMessages = new ArrayList<>();
    
    private List<String> failMessages = new ArrayList<>();
    
    public void addSuccess(String message) {
        successMessages.add(message);
        successCount++;
    }
    
    public void addFail(String message) {
        failMessages.add(message);
        failCount++;
    }
}