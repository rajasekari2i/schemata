package com.opsbeach.sharedlib.utils;

public class Base62Util {    
    public static final String ENCODEDSTR = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

	public static final int DIGITS = 7;

	public static String base62Encode(long num) {
		StringBuilder result = new StringBuilder();
		while (num > 0) {
			int reminder = (int) (num%62);
			result.append(ENCODEDSTR.charAt(reminder));
            num = num/62;
		}
		return result.substring(0, DIGITS);
	}
}
