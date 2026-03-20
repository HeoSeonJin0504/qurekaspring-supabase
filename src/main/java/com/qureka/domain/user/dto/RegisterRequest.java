package com.qureka.domain.user.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "아이디를 입력해주세요.")
    @Size(min = 5, max = 20, message = "아이디는 5-20자여야 합니다.")
    @Pattern(regexp = "^[a-z0-9]+$", message = "아이디는 영문 소문자/숫자만 사용할 수 있습니다.")
    private String userid;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(min = 8, max = 100, message = "비밀번호는 8자 이상이어야 합니다.")
    private String password;

    @NotBlank(message = "이름을 입력해주세요.")
    @Size(min = 2, max = 50, message = "이름은 2-50자여야 합니다.")
    private String name;

    @NotNull(message = "나이를 입력해주세요.")
    @Min(value = 1,   message = "나이는 1 이상이어야 합니다.")
    @Max(value = 150, message = "나이는 150 이하여야 합니다.")
    private Short age;

    @NotBlank(message = "성별을 선택해주세요.")
    @Pattern(regexp = "^(male|female)$", message = "성별은 male 또는 female이어야 합니다.")
    private String gender;

    @NotBlank(message = "전화번호를 입력해주세요.")
    @Pattern(regexp = "^01[016789]-?\\d{3,4}-?\\d{4}$", message = "전화번호 형식이 올바르지 않습니다.")
    private String phone;

    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;
}
