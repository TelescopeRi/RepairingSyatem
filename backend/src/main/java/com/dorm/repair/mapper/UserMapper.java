package com.dorm.repair.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dorm.repair.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM user WHERE role = #{role} ORDER BY create_time DESC")
    List<User> selectByRole(String role);
}