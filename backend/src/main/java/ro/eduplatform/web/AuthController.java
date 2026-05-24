package ro.eduplatform.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.eduplatform.domain.User;
import ro.eduplatform.dto.AuthDtos;
import ro.eduplatform.service.AnalyticsService;
import ro.eduplatform.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AnalyticsService analyticsService;
    private final CurrentUser currentUser;

    @PostMapping("/login")
    public ResponseEntity<AuthDtos.AuthResponse> login(@Valid @RequestBody AuthDtos.LoginRequest req) {
        AuthService.AuthCookie out = authService.login(req);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, out.cookie().toString())
                .body(out.body());
    }

    @PostMapping("/register")
    public ResponseEntity<AuthDtos.AuthResponse> register(@Valid @RequestBody AuthDtos.RegisterRequest req) {
        AuthService.AuthCookie out = authService.register(req);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, out.cookie().toString())
                .body(out.body());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, authService.logoutCookie().toString())
                .build();
    }

    @GetMapping("/me")
    public AuthDtos.AuthResponse me() {
        User u = currentUser.get();
        analyticsService.record(u, "ME_VIEW", null, null);
        return authService.toResponse(u);
    }
}
