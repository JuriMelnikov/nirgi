package ee.jvm.nirgi_java.repository;

import ee.jvm.nirgi_java.classes.Model;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModelRepository extends JpaRepository<Model, Long> {
}
