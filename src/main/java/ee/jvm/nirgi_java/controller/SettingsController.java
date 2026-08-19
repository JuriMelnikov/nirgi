package ee.jvm.nirgi_java.controller;

import ee.jvm.nirgi_java.classes.Settings;
import ee.jvm.nirgi_java.service.SettingsService;
import ee.jvm.nirgi_java.security.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SettingsController {

    private final SettingsService settingsService;

    @GetMapping
    public ResponseEntity<Settings> getSettings() {
        return ResponseEntity.ok(settingsService.getSettings());
    }

    @PutMapping("/stop-day")
    public ResponseEntity<?> updateStopDay(@RequestBody UpdateStopDayRequest request) {
        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MANAGER"))) {
            return ResponseEntity.status(403).body("Только менеджер может изменять стоп день");
        }

        try {
            Settings settings = settingsService.updateStopDay(request.stopDay());
            return ResponseEntity.ok(settings);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    public record UpdateStopDayRequest(Integer stopDay) {
    }
}
