package com.example.demo.familycard.enums;

/**
 * 메시지 카드 이미지 타입 열거형
 * 고정된 3가지 이미지 타입을 정의
 */
public enum MessageCardImageType {
    HEART("heart", "하트"),
    FLOWER("flower", "꽃"),
    STAR("star", "별");

    private final String code;
    private final String description;

    MessageCardImageType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 코드로 이미지 타입 찾기
     */
    public static MessageCardImageType fromCode(String code) {
        for (MessageCardImageType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid image type code: " + code);
    }

    /**
     * 유효한 이미지 타입 코드인지 확인
     */
    public static boolean isValidCode(String code) {
        try {
            fromCode(code);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}