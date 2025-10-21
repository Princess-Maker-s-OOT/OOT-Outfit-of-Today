package org.example.ootoutfitoftoday.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UserUpdateInfoRequest {

    /**
     * 부분 수정 허용을 위해 @NotBlank 제거 -> null 값 허용
     **/
    private static final String EMAIL_LOCAL_PART = "(?!\\.)[A-Za-z0-9._%+-]+(?<!\\.)";
    private static final String EMAIL_DOMAIN = "@[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?)*";
    private static final String EMAIL_TLD = "\\.[A-Za-z]{2,}";
    private static final String EMAIL_REGEX = "^" + EMAIL_LOCAL_PART + EMAIL_DOMAIN + EMAIL_TLD + "$";

    private static final String NICKNAME_REGEX = "^(?!\\s).*(?<!\\s)$";

    private static final String USERNAME_REGEX = "^\\S+$";

    private static final String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>])[^\\s]+$";

    private static final String PHONE_NUMBER_REGEX = "^01[016789]\\d{7,8}$";

    /**
     * TODO: 현재 제한 조건 부재(예: Blank 허용)
     *       -> 이미지 업로드 구현 후 제한 조건 추가
     **/
    private String imageUrl;

    /**
     * @NotBlank 제거
     * 입력하지 않으면 기존값 유지할 수 있도록
     **/
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Size(max = 50, message = "이메일은 최대 50자까지 가능합니다.")
    @Pattern(regexp = EMAIL_REGEX, message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @Size(min = 2, max = 10, message = "닉네임은 2~10자 사이여야 합니다.")
    @Pattern(regexp = NICKNAME_REGEX, message = "닉네임 전후 공백은 불가합니다.")
    private String nickname;

    @Size(min = 2, max = 50, message = "이름은 2~50자 사이여야 합니다.")
    @Pattern(regexp = USERNAME_REGEX, message = "이름 내 공백은 불가합니다.")
    private String username;

    @Size(min = 8, max = 30, message = "비밀번호는 8~30자 사이여야 합니다.")
    @Pattern(regexp = PASSWORD_REGEX,
            message = "비밀번호는 대소문자 불문 영문, 숫자, 특수문자를 모두 포함해야 하고 공백은 불가합니다.")
    private String password;

    @Pattern(regexp = PHONE_NUMBER_REGEX, message = "전화번호 형식은 01012345678 형태여야 합니다.")
    private String phoneNumber;
}
