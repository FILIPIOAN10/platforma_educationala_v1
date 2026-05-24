package ro.eduplatform.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import ro.eduplatform.domain.Role;

public class AuthDtos {

    public record LoginRequest(
            @NotBlank String username,
            @NotBlank String password
    ) {}

    public record RegisterRequest(
            @NotBlank @Size(min = 3, max = 80) String username,
            @NotBlank @Size(min = 4) String password,
            @NotBlank String fullName,
            @Email String email,
            Role role,
            @Min(5) @Max(8) Integer gradeLevel
    ) {}

    public record AuthResponse(
            Long id,
            String username,
            String fullName,
            Role role,
            Integer gradeLevel
    ) {}
}
