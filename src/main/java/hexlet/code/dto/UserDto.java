package hexlet.code.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Objects;

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

    // constructors

    public UserDto(String email, String firstName, String lastName, String password) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
    }

    public UserDto() {
    }

    // getters and setters

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // equals and hashCode

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserDto userDto = (UserDto) o;
        return Objects.equals(email, userDto.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, firstName, lastName, password);
    }

    // toString

    @Override
    public String toString() {
        return "UserDto{" + "email='" + email + '\'' + ", firstName='" + firstName + '\''
                + ", lastName='" + lastName + '\'' + ", password='" + password + '\'' + '}';
    }
}
