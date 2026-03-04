package com.item.dto.background;

import java.io.Serializable;

import lombok.Data;

/**
 * <p>
 *
 * </p>
 *
 * @author liuyabin on 2025/8/3
 * @since 1.0.0
 */
@Data
public class BackgroundCheckReqDTO implements Serializable {
    private String email;
    private String firstName;
    private String lastName;
    //出生日期,格式:MM-dd-yyyy
    private String dob;
    private String phone;
}
