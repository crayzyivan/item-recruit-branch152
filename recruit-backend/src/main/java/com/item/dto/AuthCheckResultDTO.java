package com.item.dto;

/**
 * @author : lh
 */
public record AuthCheckResultDTO(boolean authSuccess, Integer code, String msg) {
}
