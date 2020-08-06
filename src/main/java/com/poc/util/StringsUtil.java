package com.poc.util;

public class StringsUtil
{
    public static final String EmptyString;

    static {
        EmptyString = new String("");
    }
    
    public static String deCamelize(final String value) {
        final StringBuffer decamelized = new StringBuffer();
        int lastUCIndex = -1;
        for (int i = 0, len = value.length(); i < len; ++i) {
            char c = value.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i - 1 != lastUCIndex) {
                    decamelized.append(" ");
                }
                lastUCIndex = i;
            } else if (Character.isLowerCase(c)) {
                if (i == 0) {
                    c = Character.toUpperCase(c);
                }
            } else if (c == '_') {
                c = ' ';
            }
            decamelized.append(c);
        }
        return decamelized.toString();
    }
    
    public static boolean nullOrEmptyOrBlankString(final String str) {
        return null == str || str.equals(StringsUtil.EmptyString) || str.trim().equals(StringsUtil.EmptyString);
    }
    
    public static boolean isTestAutomationMode() {
        return true;
    }
    
}