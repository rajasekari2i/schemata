package com.opsbeach.sharedlib.exception;

/**
 * <p>
 * Added in the enum exception class.
 * </p>
 */
public class EnumValidationException extends Exception {
    private String enumName;
    private String enumValue;

    public EnumValidationException(String enumValue, String enumName) {
        super(enumValue);
        this.enumName = enumName;
        this.enumValue = enumValue;
    }

    public EnumValidationException(String enumValue, String enumName, Throwable cause) {
        super(enumValue, cause);
        this.enumName = enumName;
        this.enumValue = enumValue;
    }

    public String getEnumValue() {
        return enumValue;
    }

    public String getEnumName() {
        return enumName;
    }

}