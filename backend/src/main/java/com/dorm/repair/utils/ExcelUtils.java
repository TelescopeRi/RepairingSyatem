package com.dorm.repair.utils;

import com.dorm.repair.entity.User;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ExcelUtils {

    public static List<User> parseStudentExcel(MultipartFile file) throws IOException {
        List<User> users = new ArrayList<>();
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            // 跳过表头（第一行）
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                User user = new User();
                
                // 学号（必填）
                Cell usernameCell = row.getCell(0);
                String username = getCellStringValue(usernameCell).trim();
                if (username.isEmpty()) {
                    continue;
                }
                user.setUsername(username);
                
                // 姓名（必填）
                Cell nameCell = row.getCell(1);
                String realName = getCellStringValue(nameCell).trim();
                if (realName.isEmpty()) {
                    continue;
                }
                user.setRealName(realName);
                
                // 手机号（可选）
                Cell phoneCell = row.getCell(2);
                if (phoneCell != null) {
                    String phone = getCellStringValue(phoneCell).trim();
                    if (!phone.isEmpty()) {
                        user.setPhone(phone);
                    }
                }
                
                // 楼栋（必填）
                Cell buildingCell = row.getCell(3);
                String building = getCellStringValue(buildingCell).trim();
                if (building.isEmpty()) {
                    continue;
                }
                user.setBuilding(building);
                
                // 宿舍号（可选）
                Cell dormCell = row.getCell(4);
                if (dormCell != null) {
                    String dormNumber = getCellStringValue(dormCell).trim();
                    if (!dormNumber.isEmpty()) {
                        user.setDormNumber(dormNumber);
                    }
                }
                
                // 默认密码（学号后6位或123456）
                String password = username.length() >= 6 ? username.substring(username.length() - 6) : "123456";
                user.setPassword(PasswordUtils.encode(password));
                
                user.setRole("STUDENT");
                user.setStatus(1);
                user.setIsDeleted(0);
                
                users.add(user);
            }
        }
        return users;
    }

    public static List<User> parseRepairmanExcel(MultipartFile file) throws IOException {
        List<User> users = new ArrayList<>();
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            // 跳过表头（第一行）
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                User user = new User();
                
                // 工号（可为空，批量导入时会自动生成）
                Cell usernameCell = row.getCell(0);
                String username = getCellStringValue(usernameCell).trim();
                // 工号可以为空，后续会自动生成
                user.setUsername(username.isEmpty() ? null : username);
                
                // 姓名（必填）
                Cell nameCell = row.getCell(1);
                String realName = getCellStringValue(nameCell).trim();
                if (realName.isEmpty()) {
                    continue;  // 姓名为空则跳过该行
                }
                user.setRealName(realName);
                
                // 手机号（可选）
                Cell phoneCell = row.getCell(2);
                if (phoneCell != null) {
                    String phone = getCellStringValue(phoneCell).trim();
                    if (!phone.isEmpty()) {
                        user.setPhone(phone);
                    }
                }
                
                // 专业技能（可选）
                Cell specialtyCell = row.getCell(3);
                if (specialtyCell != null) {
                    String specialty = getCellStringValue(specialtyCell).trim();
                    if (!specialty.isEmpty()) {
                        user.setSpecialtyIds(specialty);
                    }
                }
                
                // 默认密码（批量导入时会重新设置）
                user.setPassword(PasswordUtils.encode("123456"));
                
                user.setRole("REPAIR");
                user.setStatus(1);
                user.setIsDeleted(0);
                
                users.add(user);
            }
        }
        return users;
    }

    public static List<String> parseUsernameExcel(MultipartFile file) throws IOException {
        List<String> usernames = new ArrayList<>();
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            // 跳过表头（第一行）
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                Cell cell = row.getCell(0);
                if (cell != null) {
                    String username = getCellStringValue(cell).trim();
                    if (!username.isEmpty()) {
                        usernames.add(username);
                    }
                }
            }
        }
        return usernames;
    }

    private static String getCellStringValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString();
                }
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return "";
        }
    }
}