package shootingstar.var.dto.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class UserSignupReqDto {
    @NotBlank
    private String kakaoId;
    @NotBlank
    private String userName;
    @NotBlank
    private String nickname;
    @Email
    @NotBlank
    private String email;
    private String phoneNumber;
    @URL
    @NotBlank
    private String profileImgUrl;
}
