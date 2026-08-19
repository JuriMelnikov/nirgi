package ee.jvm.nirgi_java.service;

import ee.jvm.nirgi_java.classes.Settings;
import ee.jvm.nirgi_java.repository.SettingsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettingsServiceTest {

    @Mock
    private SettingsRepository settingsRepository;

    @InjectMocks
    private SettingsService settingsService;

    @Test
    void getSettingsReturnsExistingSettings() {
        Settings existing = new Settings(10);
        when(settingsRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(existing));

        assertThat(settingsService.getSettings()).isSameAs(existing);
        verify(settingsRepository, never()).save(any());
    }

    @Test
    void getSettingsPersistsDefaultWhenNoneStored() {
        when(settingsRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());
        when(settingsRepository.save(any(Settings.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Settings created = settingsService.getSettings();

        assertThat(created.getStopDay()).isEqualTo(25);
        verify(settingsRepository).save(created);
    }

    @Test
    void getStopDayReadsFromSettings() {
        when(settingsRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(new Settings(7)));

        assertThat(settingsService.getStopDay()).isEqualTo(7);
    }

    @Test
    void updateStopDayStoresNewValue() {
        Settings existing = new Settings(10);
        when(settingsRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(existing));
        when(settingsRepository.save(existing)).thenReturn(existing);

        Settings updated = settingsService.updateStopDay(20);

        ArgumentCaptor<Settings> captor = ArgumentCaptor.forClass(Settings.class);
        verify(settingsRepository).save(captor.capture());
        assertThat(captor.getValue().getStopDay()).isEqualTo(20);
        assertThat(updated.getStopDay()).isEqualTo(20);
    }

    @Test
    void updateStopDayRejectsNullAndOutOfRangeValues() {
        assertThatThrownBy(() -> settingsService.updateStopDay(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> settingsService.updateStopDay(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> settingsService.updateStopDay(32))
                .isInstanceOf(IllegalArgumentException.class);

        verify(settingsRepository, never()).save(any());
    }
}
