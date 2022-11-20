package hexlet.code.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class UserDto {
    @NotBlank
    @Email(message = "Incorrect email format")
    private String email;

    @NotBlank
    @Size(min = 1, message = "First name must be longer than 1 character")
    private String firstName;

    @NotBlank
    @Size(min = 1, message = "Last name must be longer than 1 character")
    private String lastName;

    @NotBlank
    @Size(min = 3, message = "Password must be longer than 3 characters")
    private String password;
}
