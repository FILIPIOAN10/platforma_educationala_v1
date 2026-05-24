package ro.eduplatform.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ro.eduplatform.domain.Role;
import ro.eduplatform.domain.User;
import ro.eduplatform.dto.AuthDtos;
import ro.eduplatform.repository.UserRepository;
import ro.eduplatform.security.AppUserPrincipal;
import ro.eduplatform.security.JwtService;
import ro.eduplatform.web.ApiException;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Value("${app.jwt.cookie-name}")
    private String cookieName;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    public AuthCookie login(AuthDtos.LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password())
        );
        AppUserPrincipal principal = (AppUserPrincipal) auth.getPrincipal();
        User user = userRepository.findById(principal.getId()).orElseThrow();
        String token = jwtService.generateToken(user);
        return new AuthCookie(buildCookie(token), toResponse(user));
    }

    public AuthCookie register(AuthDtos.RegisterRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new ApiException(409, "Username deja folosit");
        }
        if (req.email() != null && !req.email().isBlank() && userRepository.existsByEmail(req.email())) {
            throw new ApiException(409, "Email deja folosit");
        }
        Role role = req.role() == null ? Role.STUDENT : req.role();
        if (role == Role.STUDENT && (req.gradeLevel() == null || req.gradeLevel() < 5 || req.gradeLevel() > 8)) {
            throw new ApiException(400, "Pentru elevi, clasa trebuie sa fie intre 5 si 8");
        }
        User user = User.builder()
                .username(req.username())
                .fullName(req.fullName())
                .email(req.email())
                .passwordHash(passwordEncoder.encode(req.password()))
                .role(role)
                .gradeLevel(role == Role.STUDENT ? req.gradeLevel() : null)
                .build();
        userRepository.save(user);
        String token = jwtService.generateToken(user);
        return new AuthCookie(buildCookie(token), toResponse(user));
    }

    public ResponseCookie logoutCookie() {
        return ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(0)
                .build();
    }

    public AuthDtos.AuthResponse toResponse(User u) {
        return new AuthDtos.AuthResponse(u.getId(), u.getUsername(), u.getFullName(), u.getRole(), u.getGradeLevel());
    }

    private ResponseCookie buildCookie(String token) {
        return ResponseCookie.from(cookieName, token)
                .httpOnly(true)
                .secure(false) // pentru MVP local; in productie -> true + SameSite=None
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ofMillis(expirationMs))
                .build();
    }

    public record AuthCookie(ResponseCookie cookie, AuthDtos.AuthResponse body) {}
}
