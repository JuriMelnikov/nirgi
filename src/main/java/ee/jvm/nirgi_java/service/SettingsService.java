package ee.jvm.nirgi_java.service;

import ee.jvm.nirgi_java.classes.Settings;
import ee.jvm.nirgi_java.repository.SettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private final SettingsRepository settingsRepository;

    public Settings getSettings() {
        return settingsRepository.findFirstByOrderByIdAsc()
                .orElseGet(() -> {
                    Settings defaultSettings = new Settings(25);
                    return settingsRepository.save(defaultSettings);
                });
    }

    @Transactional
    public Settings updateStopDay(Integer stopDay) {
        if (stopDay == null || stopDay < 1 || stopDay > 31) {
            throw new IllegalArgumentException("День должен быть от 1 до 31");
        }

        Settings settings = getSettings();
        settings.setStopDay(stopDay);
        return settingsRepository.save(settings);
    }

    public Integer getStopDay() {
        return getSettings().getStopDay();
    }
}
