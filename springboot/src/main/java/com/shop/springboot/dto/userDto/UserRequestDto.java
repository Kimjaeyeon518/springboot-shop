package com.shop.springboot.dto.userDto;

import com.shop.springboot.entity.User;
import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
@Setter
@AllArgsConstructor
@Builder
@ToString
public class UserRequestDto {

    @NotBlank(message = "이름을 작성해주세요.")
    @Size(max = 10, message = "이름은 10자 내외로 작성해주세요.")
    private String name;

    @NotBlank(message = "아이디를 작성해주세요.")
    @Pattern(regexp = "^(?=.*[a-zA-Z0-9]).{6,12}$", message = "아이디는 영문/숫자 조합 6자리 ~ 12자리")
    private String identifier;

    private String authorities;

    @NotBlank(message = "비밀번호를 작성해주세요.")
    @Pattern(regexp = "^(?=.*[a-zA-Z0-9`~!@#$%^&*()\\-_+=\\\\]).{8,15}$", message = "비밀번호는 영문/숫자/특수문자 조합 8자리~15자리")
    private String password;

    @NotBlank(message = "이메일을 작성해주세요.")
    @Email(message = "이메일의 양식을 지켜주세요.")
    private String email;

    @Size(max = 50, message = "도로명 주소를 알맞게 작성해주세요.")
    private String addr;

    @Size(max = 20, message = "상세주소를 알맞게 작성해주세요.")
    private String detailAddr;

    public User toEntity(UserRequestDto requestDto) {
        return User.builder()
                .authorities(requestDto.getAuthorities())
                .identifier(requestDto.getIdentifier())
                .password(requestDto.getPassword())
                .name(requestDto.getName())
                .email(requestDto.getEmail())
                .addr(requestDto.getAddr())
                .detailAddr(requestDto.getDetailAddr())
                .build();
    }
}
