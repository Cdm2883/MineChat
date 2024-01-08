package vip.cdms.mcoreui.util;

import java.util.UUID;

public class StringUtils {
    public static String sliceEnd(String s, int len) {
        return s.substring(0, s.length() - len);
    }
    public static String randomUUID() {
        return UUID.randomUUID().toString();
    }
}
