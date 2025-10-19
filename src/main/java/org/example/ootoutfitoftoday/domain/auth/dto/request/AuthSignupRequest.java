package org.example.ootoutfitoftoday.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class AuthSignupRequest {

    private static final String ID_REGEX = "^[a-zA-Z0-9_]+$";

    private static final String EMAIL_LOCAL_PART = "^(?!\\.)[A-Za-z0-9._%+-]+(?<!\\.)";
    private static final String EMAIL_DOMAIN = "@[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?)*";
    private static final String EMAIL_TLD = "\\.[A-Za-z]{2,}$";
    private static final String EMAIL_REGEX = EMAIL_LOCAL_PART + EMAIL_DOMAIN + EMAIL_TLD;

    private static final String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,30}$";

    private static final String PHONE_NUMBER_REGEX = "^01[016789]\\d{7,8}$";

    @NotBlank(message = "아이디는 필수 입력값입니다.")
    @Size(min = 4, max = 15, message = "아이디는 4~15자 사이여야 합니다.")
    @Pattern(regexp = ID_REGEX, message = "아이디는 영문, 숫자, 밑줄(_)만 사용할 수 있습니다.")
    private String loginId;

    @NotBlank(message = "이메일은 필수 입력값입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Size(max = 50, message = "이메일은 최대 50자까지 가능합니다.")
    @Pattern(regexp = EMAIL_REGEX, message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @NotBlank(message = "닉네임은 필수 입력값입니다.")
    @Size(min = 2, max = 10, message = "닉네임은 2~10자 사이여야 합니다.")
    private String nickname;

    @NotBlank(message = "이름은 필수 입력값입니다.")
    @Size(min = 2, max = 50, message = "이름은 2~50자 사이여야 합니다.")
    private String username;

    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    @Size(min = 8, max = 30, message = "비밀번호는 8~30자 사이여야 합니다.")
    @Pattern(regexp = PASSWORD_REGEX,
            message = "비밀번호는 최소 8자, 최대 30자이며, " +
                    "대소문자 불문 영문, 숫자, 특수문자를 모두 포함해야 합니다.")
    private String password;

    @NotBlank(message = "전화번호는 필수 입력값입니다.")
    @Pattern(regexp = PHONE_NUMBER_REGEX, message = "전화번호 형식은 01012345678 형태여야 합니다.")
    private String phoneNumber;
}
