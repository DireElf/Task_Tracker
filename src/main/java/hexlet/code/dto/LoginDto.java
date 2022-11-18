package hexlet.code.dto;

import java.util.Objects;

public class LoginDto {

    private String email;

    private String password;

    // constructors

    public LoginDto(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public LoginDto() {
    }

    // getters and setters

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // equals and hashcode

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LoginDto loginDto = (LoginDto) o;
        return Objects.equals(email, loginDto.email) && Objects.equals(password, loginDto.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, password);
    }

    // toString

    @Override
    public String toString() {
        return "LoginDto{" + "email='" + email + '\''
                + ", password='" + password + '\'' + '}';
    }
}
