package com.dorm.repair.vo;

import java.time.LocalDateTime;

public class UserVO {

    private Long id;

    private String username;

    private String realName;

    private String phone;

    private String role;

    private Integer status;

    private LocalDateTime createTime;

    private String building;

    private String dormNumber;

    private String specialtyIds;

    private String specialtyNames;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getDormNumber() {
        return dormNumber;
    }

    public void setDormNumber(String dormNumber) {
        this.dormNumber = dormNumber;
    }

    public String getSpecialtyIds() {
        return specialtyIds;
    }

    public void setSpecialtyIds(String specialtyIds) {
        this.specialtyIds = specialtyIds;
    }

    public String getSpecialtyNames() {
        return specialtyNames;
    }

    public void setSpecialtyNames(String specialtyNames) {
        this.specialtyNames = specialtyNames;
    }
}
