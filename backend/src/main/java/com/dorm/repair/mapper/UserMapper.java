package com.dorm.repair.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dorm.repair.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM `user` WHERE role = #{role} AND is_deleted = 0 ORDER BY create_time DESC")
    List<User> selectByRole(String role);
    
    @Select("SELECT COUNT(*) FROM `user` WHERE username = #{username}")
    int countByUsername(String username);
    
    @Update("UPDATE `user` SET password = #{password}, real_name = #{realName}, phone = #{phone}, role = #{role}, status = 1, building = #{building}, dorm_number = #{dormNumber}, is_deleted = 0 WHERE username = #{username}")
    int restoreUserByUsername(String username, String password, String realName, String phone, String role, String building, String dormNumber);
}