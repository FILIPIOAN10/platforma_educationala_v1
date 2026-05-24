package ro.eduplatform.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ro.eduplatform.domain.Role;
import ro.eduplatform.domain.User;

import java.util.Collection;
import java.util.List;

@Getter
@AllArgsConstructor
public class AppUserPrincipal implements UserDetails {

    private final Long id;
    private final String username;
    private final String fullName;
    private final Role role;
    private final Integer gradeLevel;
    private final String passwordHash;

    public static AppUserPrincipal from(User u) {
        return new AppUserPrincipal(
                u.getId(),
                u.getUsername(),
                u.getFullName(),
                u.getRole(),
                u.getGradeLevel(),
                u.getPasswordHash()
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
