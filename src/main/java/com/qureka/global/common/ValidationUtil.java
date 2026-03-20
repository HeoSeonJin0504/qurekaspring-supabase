package com.qureka.global.common;

import org.springframework.stereotype.Component;
import java.util.regex.Pattern;

@Component
public class ValidationUtil {

    private static final Pattern USERID_PATTERN        = Pattern.compile("^[a-z0-9]{5,20}$");
    private static final Pattern NAME_PATTERN          = Pattern.compile("^[가-힣a-zA-Z\\s]{2,50}$");
    private static final Pattern PHONE_PATTERN         = Pattern.compile("^01[016789]-?\\d{3,4}-?\\d{4}$");
    private static final Pattern EMAIL_PATTERN         = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern UNSAFE_USERID_PATTERN = Pattern.compile("[\\s;'\"`\\\\<>]");
    private static final Pattern UNSAFE_PW_PATTERN     = Pattern.compile("[\\x00-\\x1F\\x7F]");
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
            "(?i)(--|;|/\\*|\\*/|xp_|union\\s+select|drop\\s+table|insert\\s+into|delete\\s+from|update\\s+set)");

    public boolean isValidUserid(String v)  { return v != null && USERID_PATTERN.matcher(v).matches(); }
    public boolean isValidName(String v)    { return v != null && NAME_PATTERN.matcher(v).matches(); }
    public boolean isValidPhone(String v)   { return v != null && PHONE_PATTERN.matcher(v).matches(); }
    public boolean isValidEmail(String v)   { return v != null && EMAIL_PATTERN.matcher(v).matches(); }
    public boolean isValidAge(Short v)      { return v != null && v >= 1 && v <= 150; }
    public boolean isValidGender(String v)  { return "male".equals(v) || "female".equals(v); }
    public boolean isSafeUserid(String v)   { return v != null && !UNSAFE_USERID_PATTERN.matcher(v).find(); }
    public boolean isSafePassword(String v) { return v != null && !UNSAFE_PW_PATTERN.matcher(v).find(); }
    public boolean isSafeSqlInput(String v) { return v != null && !SQL_INJECTION_PATTERN.matcher(v).find(); }
}
