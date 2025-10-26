package org.example.ootoutfitoftoday.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import org.example.ootoutfitoftoday.common.constant.ValidationRegex;

@Getter
public class AuthSignupRequest {

    @NotBlank(message = "아이디는 필수 입력값입니다.")
    @Size(min = 4, max = 15, message = "아이디는 4~15자 사이여야 합니다.")
    @Pattern(regexp = ValidationRegex.ID_REGEX, message = "아이디는 영문, 숫자, 밑줄(_)만 사용할 수 있습니다.")
    private String loginId;

    @NotBlank(message = "이메일은 필수 입력값입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Size(max = 50, message = "이메일은 최대 50자까지 가능합니다.")
    @Pattern(regexp = ValidationRegex.EMAIL_REGEX, message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @NotBlank(message = "닉네임은 필수 입력값입니다.")
    @Size(min = 2, max = 10, message = "닉네임은 2~10자 사이여야 합니다.")
    @Pattern(regexp = ValidationRegex.NICKNAME_REGEX, message = "닉네임 전후 공백은 불가합니다.")
    private String nickname;

    @NotBlank(message = "이름은 필수 입력값입니다.")
    @Size(min = 2, max = 50, message = "이름은 2~50자 사이여야 합니다.")
    @Pattern(regexp = ValidationRegex.USERNAME_REGEX, message = "이름 내 공백은 불가합니다.")
    private String username;

    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    @Size(min = 8, max = 30, message = "비밀번호는 8~30자 사이여야 합니다.")
    @Pattern(regexp = ValidationRegex.PASSWORD_REGEX,
            message = "비밀번호는 대소문자 불문 영문, 숫자, 특수문자를 모두 포함해야 하고 공백은 불가합니다.")
    private String password;

    @NotBlank(message = "전화번호는 필수 입력값입니다.")
    @Pattern(regexp = ValidationRegex.PHONE_NUMBER_REGEX, message = "전화번호 형식은 01012345678 형태여야 합니다.")
    private String phoneNumber;
}
