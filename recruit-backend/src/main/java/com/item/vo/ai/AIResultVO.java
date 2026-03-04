package com.item.vo.ai;

import lombok.Data;

@Data
public class AIResultVO<T> {
    private int code;
    private T data;
}
