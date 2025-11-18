package org.example.ootoutfitoftoday.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import org.example.ootoutfitoftoday.common.constant.ValidationRegex;

@Getter
public class UserUpdateInfoRequest {

    private String imageUrl;

    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Size(max = 50, message = "이메일은 최대 50자까지 가능합니다.")
    @Pattern(regexp = ValidationRegex.EMAIL_REGEX, message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @Size(min = 2, max = 10, message = "닉네임은 2~10자 사이여야 합니다.")
    @Pattern(regexp = ValidationRegex.NICKNAME_REGEX, message = "닉네임 전후 공백은 불가합니다.")
    private String nickname;

    @Size(min = 2, max = 50, message = "이름은 2~50자 사이여야 합니다.")
    @Pattern(regexp = ValidationRegex.USERNAME_REGEX, message = "이름 내 공백은 불가합니다.")
    private String username;

    @Size(min = 8, max = 30, message = "비밀번호는 8~30자 사이여야 합니다.")
    @Pattern(regexp = ValidationRegex.PASSWORD_REGEX,
            message = "비밀번호는 대소문자 불문 영문, 숫자, 특수문자를 모두 포함해야 하고 공백은 불가합니다.")
    private String password;

    @Pattern(regexp = ValidationRegex.PHONE_NUMBER_REGEX, message = "전화번호 형식은 01012345678 형태여야 합니다.")
    private String phoneNumber;
}