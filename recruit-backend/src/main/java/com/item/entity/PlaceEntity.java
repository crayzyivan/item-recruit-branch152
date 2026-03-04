package com.item.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("r_places")
public class PlaceEntity {

    @TableId
    private Long id;

    /**
     * 对应 Google Map Place Id
     */
    private String placeId;
    /**
     * 目前支持 4 种语言
     */
    private String language;
    /**
     * 对应语言的地点名称
     */
    private String description;
    /**
     * 对应语言的地址信息，对应 Google Map Place Detail 数据
     */
    private String addressComponents;

}
