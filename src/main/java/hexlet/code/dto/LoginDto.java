package hexlet.code.dto;

import java.util.Objects;

public class LoginDto {

    private String username;

    private String password;

    // constructors

    public LoginDto(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public LoginDto() {
    }

    // getters and setters

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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
        return Objects.equals(username, loginDto.username) && Objects.equals(password, loginDto.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, password);
    }

    // toString

    @Override
    public String toString() {
        return "LoginDto{" + "username='" + username + '\''
                + ", password='" + password + '\'' + '}';
    }
}
