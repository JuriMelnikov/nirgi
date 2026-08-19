package ee.jvm.nirgi_java.repository;

import ee.jvm.nirgi_java.security.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLogin(String login);
    boolean existsByLogin(String login);

    @Modifying
    @Query(value = "DELETE FROM user_roles WHERE user_id = :userId", nativeQuery = true)
    void deleteUserRoles(@Param("userId") Long userId);

    @Modifying
    @Query(value = "INSERT INTO user_roles (user_id, role) VALUES (:userId, :role)", nativeQuery = true)
    void insertUserRole(@Param("userId") Long userId, @Param("role") String role);
}
