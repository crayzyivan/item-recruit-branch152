package com.item.pgentity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * PostgreSQL用户实体
 * 对应authentication.users表
 * 
 * UUID字段处理说明：
 * - id字段使用String类型存储UUID，配合@TableId(type = IdType.INPUT)
 * - 使用UuidUtils工具类进行String与UUID之间的转换
 * - 确保与PostgreSQL数据库的UUID类型兼容
 * 
 * @author system
 */
@Data
@TableName("authentication.users")
public class PgUser {
    
    @TableId(type = IdType.INPUT)
    private String id;
    
    @TableField("first_name")
    private String firstName;
    
    @TableField("middle_name")
    private String middleName;
    
    @TableField("last_name")
    private String lastName;
    
    @TableField("email")
    private String email;
    
    @TableField("phone")
    private String phone;
}
