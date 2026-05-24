package ro.eduplatform.web;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ro.eduplatform.domain.User;
import ro.eduplatform.repository.UserRepository;
import ro.eduplatform.security.AppUserPrincipal;

@Component
@RequiredArgsConstructor
public class CurrentUser {

    private final UserRepository userRepository;

    public User get() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof AppUserPrincipal p)) {
            throw new ApiException(401, "Neautentificat");
        }
        return userRepository.findById(p.getId())
                .orElseThrow(() -> new ApiException(401, "Sesiune invalida"));
    }
}
