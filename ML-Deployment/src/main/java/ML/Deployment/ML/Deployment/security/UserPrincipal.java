package ML.Deployment.ML.Deployment.security;

import ML.Deployment.ML.Deployment.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore; // Import if using Jackson elsewhere with UserPrincipal
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class UserPrincipal implements UserDetails {
    private String id;
    private String name; // Keep name if useful
    private String email;

    @JsonIgnore // Prevent password from being accidentally serialized
    private String password;

    private Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(String id, String name, String email, String password, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
    }

    // Factory method used by CustomUserDetailsService
    public static UserPrincipal create(User user, List<GrantedAuthority> authorities) {
        return new UserPrincipal(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPassword(),
                authorities // Use the authorities passed in
        );
    }

    // Alternative factory method if needed (maps roles internally)
    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName())) // Assuming Role has getName() method
                .collect(Collectors.toList());

        return new UserPrincipal(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPassword(),
                authorities
        );
    }


    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String getUsername() {
        return email; // Use email as the username for Spring Security
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Add logic if needed
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Add logic if needed
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Add logic if needed
    }

    @Override
    public boolean isEnabled() {
        return true; // Add logic if needed (e.g., email verification)
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPrincipal that = (UserPrincipal) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}