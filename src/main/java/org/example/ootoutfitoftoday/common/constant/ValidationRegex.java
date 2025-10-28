package org.example.ootoutfitoftoday.common.constant;

public class ValidationRegex {

    // auth & user 관련 상수
    public static final String ID_REGEX = "^[a-zA-Z0-9_]+$";

    public static final String EMAIL_LOCAL_PART = "(?!\\.)[A-Za-z0-9._%+-]+(?<!\\.)";
    public static final String EMAIL_DOMAIN = "@[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?)*";
    public static final String EMAIL_TLD = "\\.[A-Za-z]{2,}";
    public static final String EMAIL_REGEX = "^" + EMAIL_LOCAL_PART + EMAIL_DOMAIN + EMAIL_TLD + "$";

    public static final String NICKNAME_REGEX = "^(?!\\s).*(?<!\\s)$";

    public static final String USERNAME_REGEX = "^\\S+$";

    public static final String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>])[^\\s]+$";

    public static final String PHONE_NUMBER_REGEX = "^01[016789]\\d{7,8}$";

    // 인스턴스화 방지
    private ValidationRegex() {
    }
}
