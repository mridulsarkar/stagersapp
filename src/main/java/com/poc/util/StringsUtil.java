package com.poc.util;

public class StringsUtil
{
    public static final String EmptyString;

    static {
        EmptyString = new String("");
    }
    
    public static String decamelize(final String string) {
        final StringBuffer buf = new StringBuffer();
        int lastUCIndex = -1;
        for (int i = 0, len = string.length(); i < len; ++i) {
            char c = string.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i - 1 != lastUCIndex) {
                    buf.append(" ");
                }
                lastUCIndex = i;
            }
            else if (Character.isLowerCase(c)) {
                if (i == 0) {
                    c = Character.toUpperCase(c);
                }
            }
            else if (c == '_') {
                c = ' ';
            }
            buf.append(c);
        }
        return buf.toString();
    }
    
    public static boolean nullOrEmptyOrBlankString(final String str) {
        return null == str || str.equals(StringsUtil.EmptyString) || str.trim().equals(StringsUtil.EmptyString);
    }
    
    public static boolean isTestAutomationMode() {
        return true;
    }
    
}