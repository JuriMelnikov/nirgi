package ee.jvm.nirgi_java.dto;

import java.util.Set;

public class AuthResponse {
    private String token;
    private String login;
    private Set<String> roles;

    public AuthResponse() {
    }

    public AuthResponse(String token, String login, Set<String> roles) {
        this.token = token;
        this.login = login;
        this.roles = roles;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}
