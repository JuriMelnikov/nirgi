package ee.jvm.nirgi_java.repository;

import ee.jvm.nirgi_java.classes.Settings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SettingsRepository extends JpaRepository<Settings, Long> {
    Optional<Settings> findFirstByOrderByIdAsc();
}
