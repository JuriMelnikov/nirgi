package ee.jvm.nirgi_java.repository;

import ee.jvm.nirgi_java.classes.ModelList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModelListRepository extends JpaRepository<ModelList, Long> {
}
